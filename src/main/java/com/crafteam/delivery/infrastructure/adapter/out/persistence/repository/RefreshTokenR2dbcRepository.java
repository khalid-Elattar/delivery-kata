package com.crafteam.delivery.infrastructure.adapter.out.persistence.repository;

import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC repository for refresh tokens.
 */
@Repository
public interface RefreshTokenR2dbcRepository extends R2dbcRepository<RefreshTokenEntity, UUID> {

    Mono<RefreshTokenEntity> findByToken(String token);

    Flux<RefreshTokenEntity> findByUserId(UUID userId);

    Mono<Void> deleteByUserId(UUID userId);

    @Query("DELETE FROM refresh_tokens WHERE expires_at < :now")
    Mono<Void> deleteExpiredTokens(Instant now);
}
