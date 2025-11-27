package com.crafteam.delivery.application.dto.response;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response containing slot availability information.
 */
public record SlotAvailabilityResponse(
        DeliveryMode mode,
        LocalDate date,
        List<AvailableSlotInfo> availableSlots,
        RulesInfo rules
) {
    /**
     * Information about a single slot's availability.
     */
    public record AvailableSlotInfo(
            String slotId,
            LocalTime startTime,
            LocalTime endTime,
            int remainingCapacity,
            boolean available,
            String unavailabilityReason
    ) {
        public static AvailableSlotInfo available(String slotId, LocalTime startTime, LocalTime endTime, int remainingCapacity) {
            return new AvailableSlotInfo(slotId, startTime, endTime, remainingCapacity, true, null);
        }

        public static AvailableSlotInfo unavailable(String slotId, LocalTime startTime, LocalTime endTime, String reason) {
            return new AvailableSlotInfo(slotId, startTime, endTime, 0, false, reason);
        }
    }

    /**
     * Business rules information for this delivery mode.
     */
    public record RulesInfo(
            int minAdvanceHours,
            int maxAdvanceDays,
            Duration slotDuration,
            LocalTime startTime,
            LocalTime endTime,
            int defaultCapacity
    ) {}
}
