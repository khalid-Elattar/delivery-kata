package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.out.RefreshTokenRepository;
import com.crafteam.delivery.domain.model.token.RefreshToken;
import com.crafteam.delivery.domain.model.token.TokenValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for rotating refresh tokens (security best practice).
 */
@Service
public class TokenRotationService {

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRotationService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Mono<RefreshToken> rotateToken(RefreshToken oldToken) {
        // Revoke old token
        oldToken.revoke();

        // Create new token
        String newTokenValue = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        RefreshToken newToken = RefreshToken.create(
                oldToken.getUserId(),
                TokenValue.from(newTokenValue),
                expiresAt
        );

        // Save both (old marked as revoked, new as active)
        return refreshTokenRepository.save(oldToken)
                .then(refreshTokenRepository.save(newToken))
                .thenReturn(newToken);
    }
}
