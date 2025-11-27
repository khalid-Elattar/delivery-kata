package com.crafteam.delivery.domain.model.user;

import java.util.Objects;

/**
 * Value Object representing a hashed password.
 * Never stores plain text passwords, only hashed values.
 */
public record Password(String hashedValue) {

    public Password {
        Objects.requireNonNull(hashedValue, "Hashed password value cannot be null");
        if (hashedValue.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank");
        }
    }

    /**
     * Creates a Password from an already hashed value (for reconstitution from persistence).
     */
    public static Password fromHash(String hashedValue) {
        return new Password(hashedValue);
    }

    @Override
    public String toString() {
        return "Password{***}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password password)) return false;
        return hashedValue.equals(password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
}
