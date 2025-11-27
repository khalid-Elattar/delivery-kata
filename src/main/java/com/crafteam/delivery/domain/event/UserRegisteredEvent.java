package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a new user is registered.
 */
public record UserRegisteredEvent(
        UserId userId,
        Email email,
        String firstName,
        String lastName,
        Instant occurredAt
) implements DomainEvent {

    public UserRegisteredEvent {
        Objects.requireNonNull(userId, "UserId is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
