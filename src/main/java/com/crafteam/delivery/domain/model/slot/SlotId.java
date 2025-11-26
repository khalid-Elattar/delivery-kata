package com.crafteam.delivery.domain.model.slot;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique slot identifier.
 */
public record SlotId(UUID value) {

    public SlotId {
        Objects.requireNonNull(value, "SlotId value cannot be null");
    }

    public static SlotId generate() {
        return new SlotId(UUID.randomUUID());
    }

    public static SlotId from(String value) {
        return new SlotId(UUID.fromString(value));
    }

    public static SlotId from(UUID value) {
        return new SlotId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
