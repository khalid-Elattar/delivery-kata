package com.crafteam.delivery.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for booking a slot.
 */
public record BookSlotRequest(
        @NotBlank(message = "Slot ID is required")
        String slotId,

        @NotBlank(message = "Customer ID is required")
        String customerId
) {
}
