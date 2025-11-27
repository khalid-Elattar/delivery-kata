package com.crafteam.delivery.domain.exception;

import java.util.Map;

/**
 * Base exception for booking validation errors.
 * Contains error code and additional details for API error responses.
 */
public class BookingValidationException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    public BookingValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public BookingValidationException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : Map.of();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
