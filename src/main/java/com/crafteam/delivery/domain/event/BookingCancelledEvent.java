package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a booking is cancelled.
 */
public record BookingCancelledEvent(
        BookingId bookingId,
        SlotId slotId,
        int releasedCapacity,
        Instant occurredAt
) implements DomainEvent {

    public BookingCancelledEvent {
        Objects.requireNonNull(bookingId, "BookingId is required");
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
