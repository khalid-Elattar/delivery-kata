package com.crafteam.delivery.infrastructure.adapter.out.persistence.repository;

import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for UserEntity.
 */
@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserEntity, UUID> {

    Mono<UserEntity> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}
