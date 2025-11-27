package com.crafteam.delivery.interfaces.rest.dto.response;

import java.time.Instant;

/**
 * Response DTO for user data.
 */
public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String street,
        String city,
        String zipCode,
        String country,
        String phoneNumber,
        String role,
        boolean active,
        Instant createdAt
) {
}
