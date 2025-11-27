package com.crafteam.delivery.application.dto.command;

/**
 * Command for updating user profile.
 */
public record UpdateUserCommand(
        String userId,
        String firstName,
        String lastName,
        String street,
        String city,
        String zipCode,
        String country,
        String phoneNumber
) {
}
