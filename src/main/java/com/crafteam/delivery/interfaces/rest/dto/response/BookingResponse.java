package com.crafteam.delivery.interfaces.rest.dto.response;

import com.crafteam.delivery.domain.model.booking.BookingStatus;

import java.time.Instant;

/**
 * Response DTO for booking data.
 */
public record BookingResponse(
        String id,
        String slotId,
        String customerId,
        BookingStatus status,
        Instant createdAt,
        Instant confirmedAt,
        Instant cancelledAt
) {
}
