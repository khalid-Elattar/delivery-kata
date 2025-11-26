package com.crafteam.delivery.domain.model.booking;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique booking identifier.
 */
public record BookingId(UUID value) {

    public BookingId {
        Objects.requireNonNull(value, "BookingId value cannot be null");
    }

    public static BookingId generate() {
        return new BookingId(UUID.randomUUID());
    }

    public static BookingId from(String value) {
        return new BookingId(UUID.fromString(value));
    }

    public static BookingId from(UUID value) {
        return new BookingId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
