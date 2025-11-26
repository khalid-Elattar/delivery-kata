package com.crafteam.delivery.infrastructure.adapter.out.persistence.repository;

import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * R2DBC repository for BookingEntity.
 */
@Repository
public interface BookingR2dbcRepository extends R2dbcRepository<BookingEntity, UUID> {

    Flux<BookingEntity> findByCustomerId(UUID customerId);

    Flux<BookingEntity> findBySlotId(UUID slotId);
}
