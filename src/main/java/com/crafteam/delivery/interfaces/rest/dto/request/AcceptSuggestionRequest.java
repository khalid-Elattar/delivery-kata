package com.crafteam.delivery.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for accepting a suggested slot.
 */
public record AcceptSuggestionRequest(
        @NotBlank(message = "Slot ID is required")
        String slotId,

        @NotBlank(message = "User ID is required")
        String userId,

        String originalSlotId
) {
}
