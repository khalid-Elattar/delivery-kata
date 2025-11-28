package com.crafteam.delivery.application.dto.command;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command for booking a delivery slot.
 *
 * Two modes of operation:
 * 1. Legacy mode: slotId is provided (for backward compatibility)
 * 2. New mode: deliveryMode, date, and time are provided
 */
public record BookSlotCommand(
        String slotId,
        String userId,
        DeliveryMode deliveryMode,
        LocalDate date,
        LocalTime time
) {
    /**
     * Legacy constructor for backward compatibility.
     */
    public BookSlotCommand(String slotId, String userId) {
        this(slotId, userId, null, null, null);
    }

    /**
     * New constructor using mode, date, and time.
     */
    public BookSlotCommand(String userId, DeliveryMode deliveryMode, LocalDate date, LocalTime time) {
        this(null, userId, deliveryMode, date, time);
    }

    /**
     * Check if this is a legacy command (using slotId).
     */
    public boolean isLegacyMode() {
        return slotId != null;
    }
}
