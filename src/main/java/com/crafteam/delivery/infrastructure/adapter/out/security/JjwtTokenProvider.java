package com.crafteam.delivery.infrastructure.adapter.out.security;

import com.crafteam.delivery.application.dto.response.TokenClaims;
import com.crafteam.delivery.application.port.out.JwtTokenProvider;
import com.crafteam.delivery.domain.model.token.AccessToken;
import com.crafteam.delivery.domain.model.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JJWT implementation of JwtTokenProvider.
 * Wraps blocking JJWT operations in reactive Mono with boundedElastic scheduler.
 */
@Component
public class JjwtTokenProvider implements JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JjwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JjwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration-ms}") long accessTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Override
    public Mono<AccessToken> generateAccessToken(User user) {
        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(accessTokenExpirationMs);

            String token = Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail().value())
                    .claim("role", user.getRole().name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(secretKey, Jwts.SIG.HS512)
                    .compact();

            return AccessToken.of(token, expiration);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> extractUserId(String token) {
        return extractClaims(token)
                .map(TokenClaims::userId);
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<TokenClaims> extractClaims(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenClaims(
                    claims.getSubject(),
                    claims.get("email", String.class),
                    claims.get("role", String.class),
                    claims.getIssuedAt().getTime(),
                    claims.getExpiration().getTime()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
