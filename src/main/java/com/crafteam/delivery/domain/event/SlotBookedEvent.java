package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a slot is booked.
 */
public record SlotBookedEvent(
        SlotId slotId,
        BookingId bookingId,
        UserId userId,
        int remainingCapacity,
        Instant occurredAt
) implements DomainEvent {

    public SlotBookedEvent {
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(bookingId, "BookingId is required");
        Objects.requireNonNull(userId, "UserId is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
