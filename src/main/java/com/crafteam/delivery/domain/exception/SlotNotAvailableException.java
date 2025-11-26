package com.crafteam.delivery.domain.exception;

/**
 * Exception thrown when a slot is not available for booking.
 */
public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(String message) {
        super(message);
    }
}
