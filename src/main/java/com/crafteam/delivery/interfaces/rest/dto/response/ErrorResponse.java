package com.crafteam.delivery.interfaces.rest.dto.response;

import java.time.Instant;

/**
 * Response DTO for error information.
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        Instant timestamp
) {
}
