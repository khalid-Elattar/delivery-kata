package com.crafteam.delivery.infrastructure.adapter.in.web.dto.response;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for slot data.
 */
public record SlotResponse(
        String id,
        DeliveryMode deliveryMode,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        int bookedCount,
        int remainingCapacity,
        boolean available
) {
}
