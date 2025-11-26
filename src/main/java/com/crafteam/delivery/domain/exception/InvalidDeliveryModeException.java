package com.crafteam.delivery.domain.exception;

/**
 * Exception thrown when an invalid delivery mode is specified.
 */
public class InvalidDeliveryModeException extends RuntimeException {

    public InvalidDeliveryModeException(String message) {
        super(message);
    }
}
