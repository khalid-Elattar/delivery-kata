package com.crafteam.delivery.infrastructure.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC entity for refresh tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("refresh_tokens")
public class RefreshTokenEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("token")
    private String token;

    @Column("user_id")
    private UUID userId;

    @Column("issued_at")
    private Instant issuedAt;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("revoked")
    private Boolean revoked;

    @Column("revoked_at")
    private Instant revokedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public RefreshTokenEntity markAsPersisted() {
        this.isNew = false;
        return this;
    }
}
