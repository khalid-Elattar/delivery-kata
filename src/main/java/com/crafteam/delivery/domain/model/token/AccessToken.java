package com.crafteam.delivery.domain.model.token;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing a JWT access token with its expiration.
 */
public record AccessToken(String value, Instant expiresAt) {

    public AccessToken {
        Objects.requireNonNull(value, "Access token value cannot be null");
        Objects.requireNonNull(expiresAt, "Expiration time cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Access token value cannot be blank");
        }
    }

    public static AccessToken of(String value, Instant expiresAt) {
        return new AccessToken(value, expiresAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
