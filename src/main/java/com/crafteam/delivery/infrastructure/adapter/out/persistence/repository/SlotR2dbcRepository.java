package com.crafteam.delivery.infrastructure.adapter.out.persistence.repository;

import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.SlotEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC repository for SlotEntity.
 */
@Repository
public interface SlotR2dbcRepository extends R2dbcRepository<SlotEntity, UUID> {

    Flux<SlotEntity> findByDeliveryModeAndDate(String deliveryMode, LocalDate date);

    @Query("SELECT * FROM slots WHERE delivery_mode = :deliveryMode AND date = :date AND booked_count < capacity")
    Flux<SlotEntity> findAvailableByDeliveryModeAndDate(String deliveryMode, LocalDate date);

    Flux<SlotEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
