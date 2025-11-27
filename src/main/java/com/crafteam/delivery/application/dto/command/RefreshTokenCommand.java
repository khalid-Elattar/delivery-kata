package com.crafteam.delivery.application.dto.command;

/**
 * Command for refreshing access token.
 */
public record RefreshTokenCommand(String refreshToken) {
}
