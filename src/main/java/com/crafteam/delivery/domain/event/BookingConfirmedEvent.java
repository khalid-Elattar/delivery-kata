package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a booking is confirmed.
 */
public record BookingConfirmedEvent(
        BookingId bookingId,
        SlotId slotId,
        CustomerId customerId,
        Instant occurredAt
) implements DomainEvent {

    public BookingConfirmedEvent {
        Objects.requireNonNull(bookingId, "BookingId is required");
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(customerId, "CustomerId is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
