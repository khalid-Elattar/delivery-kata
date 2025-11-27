package com.crafteam.delivery.application.dto.command;

/**
 * Command for changing user password.
 */
public record ChangePasswordCommand(
        String userId,
        String oldPassword,
        String newPassword
) {
}
