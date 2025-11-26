package com.crafteam.delivery.domain.model.booking;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a customer identifier.
 */
public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "CustomerId value cannot be null");
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId from(String value) {
        return new CustomerId(UUID.fromString(value));
    }

    public static CustomerId from(UUID value) {
        return new CustomerId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
