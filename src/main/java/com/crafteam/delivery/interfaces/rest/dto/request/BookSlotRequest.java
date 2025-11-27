package com.crafteam.delivery.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for booking a slot.
 */
public record BookSlotRequest(
        @NotBlank(message = "Slot ID is required")
        String slotId,

        @NotBlank(message = "User ID is required")
        String userId
) {
}
