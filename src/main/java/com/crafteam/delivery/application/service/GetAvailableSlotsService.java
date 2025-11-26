package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.in.GetAvailableSlotsUseCase;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

/**
 * Application service for retrieving available delivery slots.
 */
@Service
public class GetAvailableSlotsService implements GetAvailableSlotsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAvailableSlotsService.class);

    private final SlotRepository slotRepository;
    private final SlotCachePort slotCache;

    public GetAvailableSlotsService(SlotRepository slotRepository, SlotCachePort slotCache) {
        this.slotRepository = slotRepository;
        this.slotCache = slotCache;
    }

    @Override
    public Flux<Slot> execute(DeliveryMode mode, LocalDate date) {
        log.debug("Getting available slots: mode={}, date={}", mode, date);

        // Try cache first
        return slotCache.getCachedSlots(mode, date)
                .switchIfEmpty(
                        slotRepository.findAvailableByDeliveryModeAndDate(mode, date)
                                .collectList()
                                .flatMapMany(slots -> {
                                    // Cache the results
                                    return slotCache.cacheSlots(mode, date, Flux.fromIterable(slots))
                                            .thenMany(Flux.fromIterable(slots));
                                })
                )
                .filter(Slot::isAvailable);
    }

    @Override
    public Flux<Slot> executeAll() {
        log.debug("Getting all slots");
        return slotRepository.findAll();
    }
}
