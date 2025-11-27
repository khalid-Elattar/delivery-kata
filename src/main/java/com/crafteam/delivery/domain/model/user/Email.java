package com.crafteam.delivery.domain.model.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address with validation.
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "Email value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email from(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
