package com.crafteam.delivery.interfaces.rest.dto.request;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record CreateSlotRequest(
        @NotNull(message = "Delivery mode is required")
        DeliveryMode deliveryMode,

        @NotEmpty(message = "Available days are required (e.g., [\"MONDAY\",\"TUESDAY\",...])")
        List<DayOfWeek> availableDays,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Min(value = 1, message = "Slot duration must be at least 1 minute")
        int slotDuration,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity
) {}
