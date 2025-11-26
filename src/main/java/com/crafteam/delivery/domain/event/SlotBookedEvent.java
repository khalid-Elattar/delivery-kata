package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a slot is booked.
 */
public record SlotBookedEvent(
        SlotId slotId,
        BookingId bookingId,
        CustomerId customerId,
        int remainingCapacity,
        Instant occurredAt
) implements DomainEvent {

    public SlotBookedEvent {
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(bookingId, "BookingId is required");
        Objects.requireNonNull(customerId, "CustomerId is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
