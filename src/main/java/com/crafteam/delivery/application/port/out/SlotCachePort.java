package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Output port for slot caching operations.
 */
public interface SlotCachePort {

    Mono<Void> cacheSlots(DeliveryMode mode, LocalDate date, Flux<Slot> slots);

    Flux<Slot> getCachedSlots(DeliveryMode mode, LocalDate date);

    Mono<Void> invalidateCache(DeliveryMode mode, LocalDate date);

    Mono<Void> invalidateAll();
}
