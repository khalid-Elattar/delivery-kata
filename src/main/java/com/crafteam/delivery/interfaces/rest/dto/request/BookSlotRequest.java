package com.crafteam.delivery.interfaces.rest.dto.request;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for booking a slot.
 *
 * Two modes of operation:
 * 1. Legacy mode: slotId is provided (for backward compatibility)
 * 2. New mode: deliveryMode, date, and time are provided
 */
public record BookSlotRequest(
        String slotId,

        // userId is extracted from JWT token, not from request body
        String userId,

        DeliveryMode deliveryMode,

        LocalDate date,

        LocalTime time
) {
    /**
     * Validate that either slotId OR (deliveryMode + date + time) are provided.
     */
    public void validate() {
        if (slotId == null && (deliveryMode == null || date == null || time == null)) {
            throw new IllegalArgumentException(
                    "Either slotId OR (deliveryMode, date, time) must be provided"
            );
        }
        if (slotId != null && (deliveryMode != null || date != null || time != null)) {
            throw new IllegalArgumentException(
                    "Provide either slotId OR (deliveryMode, date, time), not both"
            );
        }
    }

    public boolean isLegacyMode() {
        return slotId != null;
    }
}
