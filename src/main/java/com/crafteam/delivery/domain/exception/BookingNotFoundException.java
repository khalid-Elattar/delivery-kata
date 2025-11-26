package com.crafteam.delivery.domain.exception;

/**
 * Exception thrown when a booking cannot be found.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String bookingId) {
        super("Booking not found: " + bookingId);
    }
}
