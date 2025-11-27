package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.dto.response.BookingResult;
import com.crafteam.delivery.application.port.in.BookSlotWithSuggestionsUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.exception.*;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.SlotSuggestion;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.domain.service.BookingValidator;
import com.crafteam.delivery.domain.service.SlotSuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for booking delivery slots with suggestion support.
 *
 * <p>This service handles the complete booking workflow:
 * <ol>
 *   <li>Attempts to book the requested slot</li>
 *   <li>If unavailable due to capacity, returns alternative suggestions</li>
 *   <li>If user has max bookings, returns appropriate error</li>
 *   <li>All suggestions respect business rules for the delivery mode</li>
 * </ol>
 */
@Service
@Transactional
public class BookSlotWithSuggestionsService implements BookSlotWithSuggestionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(BookSlotWithSuggestionsService.class);

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;
    private final SlotCachePort slotCache;
    private final BookingValidator bookingValidator;
    private final SlotSuggestionService suggestionService;
    private final Clock clock;

    public BookSlotWithSuggestionsService(
            SlotRepository slotRepository,
            BookingRepository bookingRepository,
            EventPublisher eventPublisher,
            SlotCachePort slotCache,
            BookingValidator bookingValidator,
            SlotSuggestionService suggestionService,
            Clock clock) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.slotCache = slotCache;
        this.bookingValidator = bookingValidator;
        this.suggestionService = suggestionService;
        this.clock = clock;
    }

    @Override
    public Mono<BookingResult> execute(BookSlotCommand command) {
        log.info("Booking slot with suggestions: slotId={}, userId={}",
                command.slotId(), command.userId());

        SlotId slotId = SlotId.from(command.slotId());
        UserId userId = UserId.from(command.userId());
        LocalDateTime now = LocalDateTime.now(clock);

        return slotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(command.slotId())))
                .flatMap(slot -> processBooking(slot, userId, now));
    }

    private Mono<BookingResult> processBooking(Slot slot, UserId userId, LocalDateTime now) {
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

                    return attemptBookingOrSuggest(slot, userId, slotBookings, userBookings, now);
                });
    }

    private Mono<BookingResult> attemptBookingOrSuggest(
            Slot slot,
            UserId userId,
            List<Booking> slotBookings,
            List<Booking> userBookings,
            LocalDateTime now) {

        try {
            // Validate all business rules
            bookingValidator.validateBooking(slot, userId, slotBookings, userBookings, now);

            // If validation passes, create the booking
            return createBooking(slot, userId)
                    .map(booking -> BookingResult.confirmed(booking, slot));

        } catch (SlotNotAvailableException e) {
            // Slot is full - provide suggestions
            log.info("Slot {} is unavailable, generating suggestions", slot.getId());
            return generateSuggestions(slot, userId, userBookings, now, "FULLY_BOOKED");

        } catch (MaxActiveBookingsException e) {
            // User has too many bookings - no suggestions possible
            log.info("User {} has reached max bookings", userId);
            return Mono.just(BookingResult.maxBookingsReached(
                    slot,
                    (int) countActiveBookings(userBookings),
                    bookingValidator.getMaxActiveBookings()
            ));

        } catch (BookingValidationException e) {
            // Other validation errors - generate suggestions with specific reason
            log.info("Booking validation failed for slot {}: {}", slot.getId(), e.getMessage());
            String reason = mapExceptionToReason(e);
            return generateSuggestions(slot, userId, userBookings, now, reason);
        }
    }

    private Mono<Booking> createBooking(Slot slot, UserId userId) {
        Booking booking = slot.book(userId);

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
                )
                .doOnSuccess(b -> log.info("Booking created: {}", b.getId()));
    }

    private Mono<BookingResult> generateSuggestions(
            Slot requestedSlot,
            UserId userId,
            List<Booking> userBookings,
            LocalDateTime now,
            String unavailabilityReason) {

        DeliveryMode mode = requestedSlot.getDeliveryMode();
        LocalDate today = now.toLocalDate();

        // Determine date range for candidate slots based on mode
        LocalDate startDate = today;
        LocalDate endDate = calculateEndDate(mode, today);

        return slotRepository.findByDeliveryModeAndDate(mode, requestedSlot.getDate())
                .concatWith(fetchAdditionalCandidates(mode, requestedSlot.getDate(), startDate, endDate))
                .collectList()
                .map(candidates -> {
                    List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                            requestedSlot,
                            candidates,
                            userId,
                            userBookings,
                            now
                    );

                    if (suggestions.isEmpty()) {
                        List<String> reasons = suggestionService.explainNoSuggestions(
                                mode, now, countActiveBookings(userBookings));
                        String recommendation = suggestionService.getRecommendation(mode);
                        return BookingResult.noAlternatives(
                                requestedSlot, unavailabilityReason, reasons, recommendation);
                    }

                    return BookingResult.unavailableWithSuggestions(
                            requestedSlot, suggestions, unavailabilityReason);
                });
    }

    /**
     * Fetches additional candidate slots for days other than the requested date.
     */
    private reactor.core.publisher.Flux<Slot> fetchAdditionalCandidates(
            DeliveryMode mode,
            LocalDate requestedDate,
            LocalDate startDate,
            LocalDate endDate) {

        // For DELIVERY_TODAY and DELIVERY_ASAP, only today is valid
        if (mode == DeliveryMode.DELIVERY_TODAY || mode == DeliveryMode.DELIVERY_ASAP) {
            return reactor.core.publisher.Flux.empty();
        }

        // Fetch slots for other days in the valid range
        return slotRepository.findByDateRange(startDate, endDate)
                .filter(slot -> slot.getDeliveryMode() == mode)
                .filter(slot -> !slot.getDate().equals(requestedDate));
    }

    /**
     * Calculates the end date for candidate slot search based on delivery mode.
     */
    private LocalDate calculateEndDate(DeliveryMode mode, LocalDate today) {
        return switch (mode) {
            case DELIVERY_TODAY, DELIVERY_ASAP -> today;
            case DELIVERY -> today.plusDays(mode.getMaxAdvanceDays());
            case DRIVE -> today.plusDays(mode.getMaxAdvanceDays());
        };
    }

    /**
     * Maps exception types to reason codes.
     */
    private String mapExceptionToReason(BookingValidationException e) {
        if (e instanceof SlotInPastException) return "SLOT_IN_PAST";
        if (e instanceof InvalidDateForModeException) return "INVALID_DATE_FOR_MODE";
        if (e instanceof InvalidTimeForModeException) return "INVALID_TIME_FOR_MODE";
        if (e instanceof MinAdvanceTimeException) return "MIN_ADVANCE_TIME_NOT_MET";
        if (e instanceof MaxAdvanceDaysException) return "MAX_ADVANCE_DAYS_EXCEEDED";
        if (e instanceof CutoffTimePassedException) return "CUTOFF_TIME_PASSED";
        if (e instanceof AsapWindowExceededException) return "ASAP_WINDOW_EXCEEDED";
        if (e instanceof UserAlreadyBookedException) return "USER_ALREADY_BOOKED";
        return "VALIDATION_FAILED";
    }

    /**
     * Counts active (non-cancelled) bookings.
     */
    private long countActiveBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
    }
}
