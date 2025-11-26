package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Output port for slot persistence operations.
 */
public interface SlotRepository {

    Mono<Slot> findById(SlotId id);

    Flux<Slot> findByDeliveryModeAndDate(DeliveryMode mode, LocalDate date);

    Flux<Slot> findAvailableByDeliveryModeAndDate(DeliveryMode mode, LocalDate date);

    Flux<Slot> findByDateRange(LocalDate startDate, LocalDate endDate);

    Flux<Slot> findAll();

    Mono<Slot> save(Slot slot);

    Mono<Void> delete(SlotId id);
}
