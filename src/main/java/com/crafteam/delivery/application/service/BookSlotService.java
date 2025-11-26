package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.exception.SlotNotFoundException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for booking delivery slots.
 */
@Service
@Transactional
public class BookSlotService implements BookSlotUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookSlotService.class);

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;
    private final SlotCachePort slotCache;

    public BookSlotService(SlotRepository slotRepository,
                           BookingRepository bookingRepository,
                           EventPublisher eventPublisher,
                           SlotCachePort slotCache) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.slotCache = slotCache;
    }

    @Override
    public Mono<Booking> execute(BookSlotCommand command) {
        log.info("Booking slot: slotId={}, customerId={}",
                command.slotId(), command.customerId());

        SlotId slotId = SlotId.from(command.slotId());
        CustomerId customerId = CustomerId.from(command.customerId());

        return slotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(command.slotId())))
                .flatMap(slot -> {
                    // Domain logic - book the slot
                    Booking booking = slot.book(customerId);

                    // Persist both slot and booking
                    return slotRepository.save(slot)
                            .then(bookingRepository.save(booking))
                            .flatMap(savedBooking ->
                                    // Publish domain events
                                    eventPublisher.publishAll(slot.getDomainEvents())
                                            .doOnSuccess(v -> slot.clearDomainEvents())
                                            .thenReturn(savedBooking)
                            )
                            .flatMap(savedBooking ->
                                    // Invalidate cache
                                    slotCache.invalidateCache(slot.getDeliveryMode(), slot.getDate())
                                            .thenReturn(savedBooking)
                            );
                })
                .doOnSuccess(b -> log.info("Booking created: {}", b.getId()));
    }
}
