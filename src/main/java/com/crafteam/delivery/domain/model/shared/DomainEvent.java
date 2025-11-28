package com.crafteam.delivery.domain.model.shared;

import com.crafteam.delivery.domain.event.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

/**
 * Base interface for all domain events.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookingConfirmedEvent.class, name = "BookingConfirmed"),
        @JsonSubTypes.Type(value = BookingCancelledEvent.class, name = "BookingCancelled"),
        @JsonSubTypes.Type(value = SlotCreatedEvent.class, name = "SlotCreated"),
        @JsonSubTypes.Type(value = SlotBookedEvent.class, name = "SlotBooked"),
        @JsonSubTypes.Type(value = UserRegisteredEvent.class, name = "UserRegistered"),
        @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "UserUpdated"),
        @JsonSubTypes.Type(value = UserDeactivatedEvent.class, name = "UserDeactivated"),
        @JsonSubTypes.Type(value = RefreshTokenCreatedEvent.class, name = "RefreshTokenCreated"),
        @JsonSubTypes.Type(value = RefreshTokenRevokedEvent.class, name = "RefreshTokenRevoked")
})
public interface DomainEvent {
    Instant occurredAt();
}
