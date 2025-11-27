package com.crafteam.delivery.domain.model.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a phone number with validation.
 */
public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{8,15}$"
    );

    public PhoneNumber {
        Objects.requireNonNull(value, "Phone number value cannot be null");
        String normalized = value.replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        }
    }

    public static PhoneNumber from(String value) {
        return new PhoneNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
