package com.crafteam.delivery.domain.model.token;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a refresh token identifier.
 */
public record RefreshTokenId(UUID value) {

    public RefreshTokenId {
        Objects.requireNonNull(value, "Refresh token ID cannot be null");
    }

    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId from(String value) {
        Objects.requireNonNull(value, "Refresh token ID string cannot be null");
        try {
            return new RefreshTokenId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid refresh token ID format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
