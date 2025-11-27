package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.RefreshTokenCommand;
import com.crafteam.delivery.application.dto.response.TokenResponse;
import com.crafteam.delivery.application.port.in.RefreshAccessTokenUseCase;
import com.crafteam.delivery.application.port.out.JwtTokenProvider;
import com.crafteam.delivery.application.port.out.RefreshTokenRepository;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.InvalidTokenException;
import com.crafteam.delivery.domain.model.token.TokenValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for refreshing access tokens.
 */
@Service
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshAccessTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRotationService tokenRotationService;

    public RefreshAccessTokenService(RefreshTokenRepository refreshTokenRepository,
                                    UserRepository userRepository,
                                    JwtTokenProvider jwtTokenProvider,
                                    TokenRotationService tokenRotationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenRotationService = tokenRotationService;
    }

    @Override
    public Mono<TokenResponse> execute(RefreshTokenCommand command) {
        TokenValue tokenValue = TokenValue.from(command.refreshToken());

        return refreshTokenRepository.findByToken(tokenValue)
                .switchIfEmpty(Mono.error(new InvalidTokenException("Refresh token not found")))
                .flatMap(refreshToken -> {
                    if (!refreshToken.isValid()) {
                        return Mono.error(new InvalidTokenException("Refresh token is invalid or expired"));
                    }

                    // Rotate refresh token (security best practice)
                    return tokenRotationService.rotateToken(refreshToken)
                            .flatMap(newRefreshToken ->
                                    userRepository.findById(refreshToken.getUserId())
                                            .flatMap(user ->
                                                    jwtTokenProvider.generateAccessToken(user)
                                                            .map(accessToken -> TokenResponse.of(
                                                                    accessToken.value(),
                                                                    newRefreshToken.getToken().value(),
                                                                    3600, // 1 hour in seconds
                                                                    user.getId().toString(),
                                                                    user.getEmail().value(),
                                                                    user.getRole().name()
                                                            ))
                                            )
                            );
                })
                .doOnSuccess(response -> log.info("Access token refreshed successfully"))
                .doOnError(e -> log.error("Error refreshing access token", e));
    }
}
