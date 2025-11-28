package com.crafteam.delivery.infrastructure.adapter.out.persistence.repository;

import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.SlotEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * R2DBC repository for SlotEntity.
 * Slots are now availability templates (not specific to dates).
 */
@Repository
public interface SlotR2dbcRepository extends R2dbcRepository<SlotEntity, UUID> {

    /**
     * Find the slot template for a specific delivery mode.
     */
    @Query("SELECT * FROM slots WHERE delivery_mode = :deliveryMode")
    Mono<SlotEntity> findByDeliveryMode(String deliveryMode);
}
