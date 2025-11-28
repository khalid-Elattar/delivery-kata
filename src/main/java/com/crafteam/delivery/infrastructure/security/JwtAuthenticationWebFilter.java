package com.crafteam.delivery.infrastructure.security;

import com.crafteam.delivery.application.port.out.JwtTokenProvider;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.model.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * WebFlux filter for JWT authentication.
 * Extracts JWT from Authorization header, validates it, and sets authentication.
 */
@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationWebFilter(JwtTokenProvider jwtTokenProvider,
                                     UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange);

        if (token == null) {
            return chain.filter(exchange);
        }

        return jwtTokenProvider.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return chain.filter(exchange);
                    }

                    return jwtTokenProvider.extractUserId(token)
                            .flatMap(userId ->
                                    userRepository.findById(UserId.from(userId))
                                            .flatMap(user -> {
                                                CustomUserDetails userDetails = new CustomUserDetails(user);

                                                UsernamePasswordAuthenticationToken authentication =
                                                        new UsernamePasswordAuthenticationToken(
                                                                userDetails,
                                                                null,
                                                                userDetails.getAuthorities()
                                                        );

                                                return chain.filter(exchange)
                                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                                            })
                            )
                            .switchIfEmpty(chain.filter(exchange));
                })
                .onErrorResume(e -> {
                    log.error("Error processing JWT token", e);
                    return chain.filter(exchange);
                });
    }

    private String extractToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
