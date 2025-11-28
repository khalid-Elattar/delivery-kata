package com.crafteam.delivery.application.dto.command;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Command for creating a new delivery slot template.
 * Updated for template-based architecture (no specific date).
 */
public record CreateSlotCommand(
        DeliveryMode deliveryMode,
        List<DayOfWeek> availableDays,
        LocalTime startTime,
        LocalTime endTime,
        int slotDuration,
        int capacity
) {
}
