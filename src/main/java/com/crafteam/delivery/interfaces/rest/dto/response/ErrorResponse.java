package com.crafteam.delivery.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for error information with optional details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        Instant timestamp,
        Map<String, Object> details
) {
    public ErrorResponse(int status, String code, String message, Instant timestamp) {
        this(status, code, message, timestamp, null);
    }
}
