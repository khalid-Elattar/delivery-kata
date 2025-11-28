package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.application.port.in.CreateSlotUseCase;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for creating delivery slots.
 */
@Service
@Transactional
public class CreateSlotService implements CreateSlotUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSlotService.class);

    private final SlotRepository slotRepository;
    private final EventPublisher eventPublisher;
    private final SlotCachePort slotCache;

    public CreateSlotService(SlotRepository slotRepository,
                             EventPublisher eventPublisher,
                             SlotCachePort slotCache) {
        this.slotRepository = slotRepository;
        this.eventPublisher = eventPublisher;
        this.slotCache = slotCache;
    }

    @Override
    public Mono<Slot> execute(CreateSlotCommand command) {
        log.info("Creating slot template: mode={}, time={}-{}",
                command.deliveryMode(), command.startTime(), command.endTime());

        // TODO: Update CreateSlotCommand to accept Set<DayOfWeek> and Duration instead of LocalDate
        // For now, create a slot using the delivery mode's defaults
        // This is a temporary workaround until the command is updated

        Slot slot = Slot.createFromMode(command.deliveryMode());

        return slotRepository.save(slot)
                .flatMap(savedSlot ->
                        eventPublisher.publishAll(savedSlot.getDomainEvents())
                                .doOnSuccess(v -> savedSlot.clearDomainEvents())
                                .thenReturn(savedSlot)
                )
                // TODO: Cache invalidation needs to be reworked for templates
                // .flatMap(savedSlot ->
                //         slotCache.invalidateCache(command.deliveryMode(), command.date())
                //                 .thenReturn(savedSlot)
                // )
                .doOnSuccess(s -> log.info("Slot template created: {}", s.getId()));
    }
}
