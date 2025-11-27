package com.crafteam.delivery.application.dto.query;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;

/**
 * Query for slot availability.
 */
public record SlotAvailabilityQuery(
        DeliveryMode mode,
        LocalDate date
) {
    public SlotAvailabilityQuery {
        if (mode == null) {
            throw new IllegalArgumentException("Delivery mode is required");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
    }
}
