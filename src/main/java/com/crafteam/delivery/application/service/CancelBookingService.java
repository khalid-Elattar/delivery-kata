package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.CancelBookingCommand;
import com.crafteam.delivery.application.port.in.CancelBookingUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.exception.BookingNotFoundException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.service.BookingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Application service for cancelling bookings.
 * Validates cancellation rules (at least 1 hour before slot start).
 */
@Service
@Transactional
public class CancelBookingService implements CancelBookingUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelBookingService.class);

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final EventPublisher eventPublisher;
    private final SlotCachePort slotCache;
    private final BookingValidator bookingValidator;
    private final Clock clock;

    public CancelBookingService(BookingRepository bookingRepository,
                                SlotRepository slotRepository,
                                EventPublisher eventPublisher,
                                SlotCachePort slotCache,
                                BookingValidator bookingValidator,
                                Clock clock) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.eventPublisher = eventPublisher;
        this.slotCache = slotCache;
        this.bookingValidator = bookingValidator;
        this.clock = clock;
    }

    @Override
    public Mono<Void> execute(CancelBookingCommand command) {
        log.info("Cancelling booking: {}", command.bookingId());

        BookingId bookingId = BookingId.from(command.bookingId());
        LocalDateTime now = LocalDateTime.now(clock);

        return bookingRepository.findById(bookingId)
                .switchIfEmpty(Mono.error(new BookingNotFoundException(command.bookingId())))
                .flatMap(booking -> validateAndCancel(booking, now))
                .doOnSuccess(v -> log.info("Booking cancelled: {}", command.bookingId()));
    }

    private Mono<Void> validateAndCancel(Booking booking, LocalDateTime now) {
        return slotRepository.findById(booking.getSlotId())
                .flatMap(slot -> {
                    // Calculate slot start time
                    LocalDateTime slotStartTime = LocalDateTime.of(
                            slot.getDate(),
                            slot.getTimeSlot().startTime()
                    );

                    // RG07: Validate cancellation is allowed (at least 1h before slot)
                    bookingValidator.validateCancellationAllowed(booking, slotStartTime, now);

                    // Cancel the booking
                    booking.cancel();

                    // Release the slot
                    slot.releaseBooking();

                    return persistCancellation(booking, slot);
                });
    }

    private Mono<Void> persistCancellation(Booking booking, Slot slot) {
        return slotRepository.save(slot)
                .then(bookingRepository.save(booking))
                .then(eventPublisher.publishAll(booking.getDomainEvents()))
                .doOnSuccess(v -> booking.clearDomainEvents())
                .then(slotCache.invalidateCache(slot.getDeliveryMode(), slot.getDate()));
    }
}
