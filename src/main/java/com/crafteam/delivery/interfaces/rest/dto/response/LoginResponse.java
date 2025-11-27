package com.crafteam.delivery.interfaces.rest.dto.response;

/**
 * Response DTO for login.
 */
public record LoginResponse(
        String userId,
        String email,
        String firstName,
        String lastName,
        String role,
        String message
) {
}
