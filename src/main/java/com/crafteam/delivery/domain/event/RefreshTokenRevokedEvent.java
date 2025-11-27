package com.crafteam.delivery.domain.event;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.token.RefreshTokenId;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a refresh token is revoked.
 */
public record RefreshTokenRevokedEvent(
        RefreshTokenId tokenId,
        UserId userId,
        Instant occurredAt
) implements DomainEvent {

    public RefreshTokenRevokedEvent {
        Objects.requireNonNull(tokenId, "Token ID is required");
        Objects.requireNonNull(userId, "User ID is required");
        Objects.requireNonNull(occurredAt, "Occurred at timestamp is required");
    }
}
