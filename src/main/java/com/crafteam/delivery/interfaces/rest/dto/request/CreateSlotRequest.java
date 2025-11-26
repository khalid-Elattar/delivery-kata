package com.crafteam.delivery.interfaces.rest.dto.request;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating a slot.
 */
public record CreateSlotRequest(
        @NotNull(message = "Delivery mode is required")
        DeliveryMode deliveryMode,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity
) {
}
