package com.crafteam.delivery.domain.model.token;

import java.util.Objects;

/**
 * Value object representing a token string (for refresh tokens).
 */
public record TokenValue(String value) {

    public TokenValue {
        Objects.requireNonNull(value, "Token value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Token value cannot be blank");
        }
    }

    public static TokenValue from(String value) {
        return new TokenValue(value);
    }
}
