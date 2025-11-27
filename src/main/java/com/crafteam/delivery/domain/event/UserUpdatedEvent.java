package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user's profile is updated.
 */
public record UserUpdatedEvent(
        UserId userId,
        Email email,
        Instant occurredAt
) implements DomainEvent {

    public UserUpdatedEvent {
        Objects.requireNonNull(userId, "UserId is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
