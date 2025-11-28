package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.SlotId;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

/**
 * Domain event raised when a new slot template is created.
 *
 * In the new architecture, slots are templates that define availability patterns,
 * not specific bookings for specific dates.
 */
public record SlotCreatedEvent(
        SlotId slotId,
        DeliveryMode deliveryMode,
        Set<DayOfWeek> availableDays,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        Instant occurredAt
) implements DomainEvent {

    public SlotCreatedEvent {
        Objects.requireNonNull(slotId, "SlotId is required");
        Objects.requireNonNull(deliveryMode, "DeliveryMode is required");
        Objects.requireNonNull(availableDays, "Available days is required");
        Objects.requireNonNull(startTime, "Start time is required");
        Objects.requireNonNull(endTime, "End time is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }
}
