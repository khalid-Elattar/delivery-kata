package com.crafteam.delivery.application.dto.command;

/**
 * Command for user login.
 */
public record LoginCommand(
        String email,
        String password
) {
}
