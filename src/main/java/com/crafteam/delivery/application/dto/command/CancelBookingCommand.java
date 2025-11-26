package com.crafteam.delivery.application.dto.command;

/**
 * Command for cancelling a booking.
 */
public record CancelBookingCommand(
        String bookingId
) {
}
