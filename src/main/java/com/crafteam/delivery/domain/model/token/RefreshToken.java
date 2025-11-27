package com.crafteam.delivery.domain.model.token;

import com.crafteam.delivery.domain.event.RefreshTokenCreatedEvent;
import com.crafteam.delivery.domain.event.RefreshTokenRevokedEvent;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for refresh tokens.
 * Manages the lifecycle of refresh tokens with revocation support.
 */
public class RefreshToken {

    private final RefreshTokenId id;
    private final TokenValue token;
    private final UserId userId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean revoked;
    private Instant revokedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private RefreshToken(RefreshTokenId id, TokenValue token, UserId userId,
                        Instant issuedAt, Instant expiresAt, boolean revoked, Instant revokedAt) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
    }

    /**
     * Factory method to create a new refresh token.
     */
    public static RefreshToken create(UserId userId, TokenValue token, Instant expiresAt) {
        Objects.requireNonNull(userId, "User ID is required");
        Objects.requireNonNull(token, "Token is required");
        Objects.requireNonNull(expiresAt, "Expiration time is required");

        Instant now = Instant.now();
        if (expiresAt.isBefore(now)) {
            throw new IllegalArgumentException("Expiration must be in the future");
        }

        RefreshTokenId id = RefreshTokenId.generate();
        RefreshToken refreshToken = new RefreshToken(
                id,
                token,
                userId,
                now,
                expiresAt,
                false,
                null
        );

        refreshToken.domainEvents.add(new RefreshTokenCreatedEvent(id, userId, now));

        return refreshToken;
    }

    /**
     * Reconstitutes a refresh token from persistence.
     */
    public static RefreshToken reconstitute(RefreshTokenId id, TokenValue token, UserId userId,
                                           Instant issuedAt, Instant expiresAt,
                                           boolean revoked, Instant revokedAt) {
        return new RefreshToken(id, token, userId, issuedAt, expiresAt, revoked, revokedAt);
    }

    /**
     * Revokes this refresh token.
     */
    public void revoke() {
        if (this.revoked) {
            throw new IllegalStateException("Token is already revoked");
        }
        this.revoked = true;
        this.revokedAt = Instant.now();

        domainEvents.add(new RefreshTokenRevokedEvent(this.id, this.userId, this.revokedAt));
    }

    /**
     * Checks if the token is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token is valid (not revoked and not expired).
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    // Getters
    public RefreshTokenId getId() {
        return id;
    }

    public TokenValue getToken() {
        return token;
    }

    public UserId getUserId() {
        return userId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
