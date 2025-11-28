package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.exception.MaxActiveBookingsException;
import com.crafteam.delivery.domain.exception.SlotNotAvailableException;
import com.crafteam.delivery.domain.exception.SlotNotFoundException;
import com.crafteam.delivery.domain.exception.UserAlreadyBookedException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.domain.service.BookingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Application service for booking delivery slots.
 * Uses the new slot template architecture.
 */
@Service
@Transactional
public class BookSlotService implements BookSlotUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookSlotService.class);
    private static final int MAX_ACTIVE_BOOKINGS = 3;

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;
    private final BookingValidator bookingValidator;

    public BookSlotService(SlotRepository slotRepository,
                           BookingRepository bookingRepository,
                           EventPublisher eventPublisher,
                           BookingValidator bookingValidator) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.bookingValidator = bookingValidator;
    }

    @Override
    public Mono<Booking> execute(BookSlotCommand command) {
        DeliveryMode mode = command.deliveryMode();
        LocalDate bookingDate = command.date();
        LocalTime bookingTime = command.time();
        String userId = command.userId();
        LocalDateTime now = LocalDateTime.now();

        log.info("Booking request: mode={}, date={}, time={}, userId={}",
                mode, bookingDate, bookingTime, userId);

        // 1. Find the slot template for this delivery mode
        return slotRepository.findByDeliveryMode(mode)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(
                        String.format("No slot template found for mode: %s", mode)
                )))

                // 2. Validate all business rules
                .doOnNext(slot -> {
                    log.debug("Found slot template: {}", slot);
                    bookingValidator.validate(slot, bookingDate, bookingTime, now);
                })

                // 3. Check capacity and create booking
                .flatMap(slot -> checkCapacityAndBook(slot, bookingDate, bookingTime, userId));
    }

    /**
     * Check capacity for the specific date/time and create booking if available.
     */
    private Mono<Booking> checkCapacityAndBook(Slot slot, LocalDate date, LocalTime time, String userId) {
        // Count existing bookings for this slot template + specific date/time
        return bookingRepository.countBySlotIdAndDateAndTime(slot.getId(), date, time)
                .flatMap(currentBookings -> {
                    log.debug("Current bookings for {}/{}/{}: {}/{}",
                            slot.getDeliveryMode(), date, time, currentBookings, slot.getCapacity());

                    // Check capacity
                    if (currentBookings >= slot.getCapacity()) {
                        log.info("Slot is full: {}/{} bookings", currentBookings, slot.getCapacity());
                        return Mono.error(new SlotNotAvailableException(
                                String.format("Slot is fully booked (%d/%d)",
                                        currentBookings, slot.getCapacity())
                        ));
                    }

                    // Check user hasn't already booked this slot/date/time
                    return bookingRepository.existsBySlotIdAndUserIdAndDateAndTime(
                                    slot.getId(), UserId.from(userId), date, time
                            )
                            .flatMap(alreadyBooked -> {
                                if (alreadyBooked) {
                                    log.info("User {} already has a booking for {}/{}/{}",
                                            userId, slot.getId(), date, time);
                                    return Mono.error(new UserAlreadyBookedException(
                                            UserId.from(userId), slot.getId()
                                    ));
                                }

                                // Check user's total active bookings
                                return checkUserBookingLimitAndCreate(slot, date, time, userId);
                            });
                });
    }

    /**
     * Check user's active bookings limit and create booking.
     */
    private Mono<Booking> checkUserBookingLimitAndCreate(Slot slot, LocalDate date,
                                                          LocalTime time, String userId) {
        return bookingRepository.countActiveByUserId(UserId.from(userId))
                .flatMap(activeCount -> {
                    log.debug("User {} has {} active bookings", userId, activeCount);

                    // RG08: Maximum 3 active bookings per user
                    if (activeCount >= MAX_ACTIVE_BOOKINGS) {
                        log.info("User {} has reached max active bookings: {}/{}",
                                userId, activeCount, MAX_ACTIVE_BOOKINGS);
                        return Mono.error(new MaxActiveBookingsException(
                                UserId.from(userId), activeCount.intValue(), MAX_ACTIVE_BOOKINGS
                        ));
                    }

                    return createBooking(slot, date, time, userId);
                });
    }

    /**
     * Create and save the booking.
     */
    private Mono<Booking> createBooking(Slot slot, LocalDate date, LocalTime time, String userId) {
        LocalTime endTime = slot.calculateEndTime(time);

        log.info("Creating booking: mode={}, date={}, time={}-{}, userId={}",
                slot.getDeliveryMode(), date, time, endTime, userId);

        // Create booking domain object
        Booking booking = Booking.create(
                slot.getId(),
                UserId.from(userId),
                date,
                time
        );

        // Confirm the booking (state transition)
        booking.confirm();

        // Save and publish events
        return bookingRepository.save(booking)
                .doOnSuccess(saved -> {
                    log.info("Booking created successfully: id={}, slotId={}, date={}, time={}-{}",
                            saved.getId(), slot.getId(), date, time, endTime);

                    // Publish domain events
                    if (!booking.getDomainEvents().isEmpty()) {
                        eventPublisher.publishAll(booking.getDomainEvents()).subscribe();
                        booking.clearDomainEvents();
                    }
                })
                .doOnError(error -> log.error("Failed to create booking", error));
    }
}
