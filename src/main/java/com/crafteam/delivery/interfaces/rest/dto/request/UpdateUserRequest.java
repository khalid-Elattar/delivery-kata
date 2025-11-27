package com.crafteam.delivery.interfaces.rest.dto.request;

/**
 * Request DTO for updating user profile.
 */
public record UpdateUserRequest(
        String firstName,
        String lastName,
        String street,
        String city,
        String zipCode,
        String country,
        String phoneNumber
) {
}
