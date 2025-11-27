package com.crafteam.delivery.application.dto.response;

/**
 * DTO representing JWT token claims.
 */
public record TokenClaims(
        String userId,
        String email,
        String role,
        long issuedAt,
        long expiresAt
) {
}
