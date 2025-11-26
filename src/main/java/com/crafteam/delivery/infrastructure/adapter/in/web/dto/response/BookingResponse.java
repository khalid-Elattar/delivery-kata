package com.crafteam.delivery.infrastructure.adapter.in.web.dto.response;

import com.crafteam.delivery.domain.model.booking.Booking;
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
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId().toString(),
                booking.getSlotId().toString(),
                booking.getCustomerId().toString(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getConfirmedAt(),
                booking.getCancelledAt()
        );
    }
}
