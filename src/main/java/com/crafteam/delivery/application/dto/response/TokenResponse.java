package com.crafteam.delivery.application.dto.response;

/**
 * Response DTO for authentication endpoints returning JWT tokens.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String userId,
        String email,
        String role
) {
    public static TokenResponse of(String accessToken, String refreshToken,
                                   long expiresIn, String userId,
                                   String email, String role) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                userId,
                email,
                role
        );
    }
}
