package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.TimeSlot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain event raised when a new slot is created.
 */
public record SlotCreatedEvent(
        SlotId slotId,
        DeliveryMode deliveryMode,
        LocalDate date,
        TimeSlot timeSlot,
        int capacity,
        Instant occurredAt
) implements DomainEvent {

    public SlotCreatedEvent {
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(deliveryMode, "DeliveryMode is required");
        Objects.requireNonNull(date, "Date is required");
        Objects.requireNonNull(timeSlot, "TimeSlot is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
