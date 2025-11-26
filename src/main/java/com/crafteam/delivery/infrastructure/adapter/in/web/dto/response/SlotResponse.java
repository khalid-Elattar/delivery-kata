package com.crafteam.delivery.infrastructure.adapter.in.web.dto.response;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;

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
    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId().toString(),
                slot.getDeliveryMode(),
                slot.getDate(),
                slot.getTimeSlot().startTime(),
                slot.getTimeSlot().endTime(),
                slot.getCapacity(),
                slot.getBookedCount(),
                slot.remainingCapacity(),
                slot.isAvailable()
        );
    }
}
