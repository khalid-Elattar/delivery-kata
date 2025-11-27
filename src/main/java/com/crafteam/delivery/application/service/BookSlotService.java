package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.exception.SlotNotFoundException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.domain.service.BookingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for booking delivery slots.
 * Validates all business rules before creating a booking.
 */
@Service
@Transactional
public class BookSlotService implements BookSlotUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookSlotService.class);

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;
    private final SlotCachePort slotCache;
    private final BookingValidator bookingValidator;
    private final Clock clock;

    public BookSlotService(SlotRepository slotRepository,
                           BookingRepository bookingRepository,
                           EventPublisher eventPublisher,
                           SlotCachePort slotCache,
                           BookingValidator bookingValidator,
                           Clock clock) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.slotCache = slotCache;
        this.bookingValidator = bookingValidator;
        this.clock = clock;
    }

    @Override
    public Mono<Booking> execute(BookSlotCommand command) {
        log.info("Booking slot: slotId={}, userId={}",
                command.slotId(), command.userId());

        SlotId slotId = SlotId.from(command.slotId());
        UserId userId = UserId.from(command.userId());
        LocalDateTime now = LocalDateTime.now(clock);

        return slotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(command.slotId())))
                .flatMap(slot -> validateAndBook(slot, userId, now));
    }

    private Mono<Booking> validateAndBook(Slot slot, UserId userId, LocalDateTime now) {
        // Fetch existing bookings for validation
        Mono<List<Booking>> slotBookingsMono = bookingRepository.findBySlotId(slot.getId())
                .collectList();
        Mono<List<Booking>> userBookingsMono = bookingRepository.findByUserId(userId)
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collectList();

        return Mono.zip(slotBookingsMono, userBookingsMono)
                .flatMap(tuple -> {
                    List<Booking> slotBookings = tuple.getT1();
                    List<Booking> userBookings = tuple.getT2();

                    // Validate all business rules
                    bookingValidator.validateBooking(slot, userId, slotBookings, userBookings, now);

                    // Create the booking
                    Booking booking = slot.book(userId);

                    // Persist both slot and booking
                    return slotRepository.save(slot)
                            .then(bookingRepository.save(booking))
                            .flatMap(savedBooking ->
                                    eventPublisher.publishAll(slot.getDomainEvents())
                                            .doOnSuccess(v -> slot.clearDomainEvents())
                                            .thenReturn(savedBooking)
                            )
                            .flatMap(savedBooking ->
                                    slotCache.invalidateCache(slot.getDeliveryMode(), slot.getDate())
                                            .thenReturn(savedBooking)
                            );
                })
                .doOnSuccess(b -> log.info("Booking created: {}", b.getId()));
    }
}
