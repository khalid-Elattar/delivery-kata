package com.crafteam.delivery.application.dto.command;

/**
 * Command for registering a new user.
 */
public record RegisterUserCommand(
        String firstName,
        String lastName,
        String email,
        String password,
        String street,
        String city,
        String zipCode,
        String country,
        String phoneNumber
) {
}
