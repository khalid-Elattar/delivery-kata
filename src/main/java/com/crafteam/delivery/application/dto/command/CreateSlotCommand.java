package com.crafteam.delivery.application.dto.command;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command for creating a new delivery slot.
 */
public record CreateSlotCommand(
        DeliveryMode deliveryMode,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int capacity
) {
}
