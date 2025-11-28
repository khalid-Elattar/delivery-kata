package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.exception.*;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Domain service responsible for validating booking business rules.
 * Works with the new slot template architecture.
 */
@Component
public class BookingValidator {

    /**
     * Validate a booking request against slot template rules.
     *
     * @param slot The slot template
     * @param bookingDate The requested booking date
     * @param bookingTime The requested booking time
     * @param now Current date/time for validation
     * @throws BookingValidationException if any validation rule fails
     */
    public void validate(Slot slot, LocalDate bookingDate, LocalTime bookingTime, LocalDateTime now) {
        // RG02: Pas de réservation dans le passé
        validateNotInPast(bookingDate, bookingTime, now);

        // RG04: Validate day is available for this slot template
        validateDayAvailable(slot, bookingDate);

        // RG05: Validate time is valid for this slot template
        validateTimeValid(slot, bookingTime);

        DeliveryMode mode = slot.getDeliveryMode();

        // Validate minimum advance time
        validateMinAdvanceTime(mode, bookingDate, bookingTime, now);

        // Validate maximum advance days
        validateMaxAdvanceDays(mode, bookingDate, now.toLocalDate());

        // Mode-specific validations
        validateModeSpecificRules(mode, bookingDate, bookingTime, now);
    }

    /**
     * RG02: Validates that the booking is not in the past.
     */
    private void validateNotInPast(LocalDate date, LocalTime time, LocalDateTime now) {
        LocalDateTime bookingDateTime = LocalDateTime.of(date, time);
        if (bookingDateTime.isBefore(now)) {
            throw new SlotInPastException(date, time);
        }
    }

    /**
     * RG04: Validates that the booking date's day of week is available for the slot template.
     */
    private void validateDayAvailable(Slot slot, LocalDate bookingDate) {
        DayOfWeek dayOfWeek = bookingDate.getDayOfWeek();
        if (!slot.isAvailableOn(dayOfWeek)) {
            throw new InvalidDateForModeException(
                    slot.getDeliveryMode(),
                    bookingDate,
                    slot.getAvailableDays()
            );
        }
    }

    /**
     * RG05: Validates that the booking time is valid for the slot template.
     * - Must be within operating hours
     * - Must align with slot grid (e.g., for 60min slots: 08:00, 09:00, not 08:30)
     */
    private void validateTimeValid(Slot slot, LocalTime bookingTime) {
        if (!slot.isValidBookingTime(bookingTime)) {
            throw new InvalidTimeForModeException(
                    slot.getDeliveryMode(),
                    bookingTime,
                    slot.getStartTime(),
                    slot.getEndTime(),
                    slot.getValidBookingTimes()
            );
        }
    }

    /**
     * Validates minimum advance time requirement.
     */
    private void validateMinAdvanceTime(DeliveryMode mode, LocalDate bookingDate,
                                        LocalTime bookingTime, LocalDateTime now) {
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);

        // Special handling for ASAP (30 minutes minimum)
        if (mode == DeliveryMode.DELIVERY_ASAP) {
            Duration minAdvance = Duration.ofMinutes(30);
            if (Duration.between(now, bookingDateTime).compareTo(minAdvance) < 0) {
                throw new MinAdvanceTimeException(mode, bookingDateTime, now);
            }
            return;
        }

        // Other modes use hours
        Duration minAdvance = Duration.ofHours(mode.getMinAdvanceHours());
        if (Duration.between(now, bookingDateTime).compareTo(minAdvance) < 0) {
            throw new MinAdvanceTimeException(mode, bookingDateTime, now);
        }
    }

    /**
     * Validates maximum advance days requirement.
     */
    private void validateMaxAdvanceDays(DeliveryMode mode, LocalDate bookingDate, LocalDate today) {
        // ASAP uses hours, not days
        if (mode == DeliveryMode.DELIVERY_ASAP) {
            return;
        }

        long daysAhead = ChronoUnit.DAYS.between(today, bookingDate);
        int maxDays = mode.getMaxAdvanceDays();

        if (daysAhead > maxDays) {
            throw new MaxAdvanceDaysException(mode, bookingDate, today);
        }
    }

    /**
     * Validate mode-specific rules.
     */
    private void validateModeSpecificRules(DeliveryMode mode, LocalDate bookingDate,
                                           LocalTime bookingTime, LocalDateTime now) {
        switch (mode) {
            case DELIVERY_TODAY -> validateDeliveryToday(bookingDate, now);
            case DELIVERY_ASAP -> validateDeliveryAsap(bookingDate, bookingTime, now);
            default -> {
                // No additional rules for DRIVE and DELIVERY
            }
        }
    }

    /**
     * DELIVERY_TODAY specific validation:
     * - Can only book for today
     * - Cutoff time is 19:00
     */
    private void validateDeliveryToday(LocalDate bookingDate, LocalDateTime now) {
        if (!bookingDate.equals(now.toLocalDate())) {
            throw new InvalidDateForModeException(
                    DeliveryMode.DELIVERY_TODAY,
                    bookingDate,
                    "DELIVERY_TODAY can only be booked for today"
            );
        }

        // Cutoff time: 19:00
        if (now.toLocalTime().isAfter(LocalTime.of(19, 0)) ||
                now.toLocalTime().equals(LocalTime.of(19, 0))) {
            throw new CutoffTimePassedException(DeliveryMode.DELIVERY_TODAY, LocalTime.of(19, 0));
        }
    }

    /**
     * DELIVERY_ASAP specific validation:
     * - Can only book for today
     * - Maximum 4 hours ahead
     */
    private void validateDeliveryAsap(LocalDate bookingDate, LocalTime bookingTime, LocalDateTime now) {
        if (!bookingDate.equals(now.toLocalDate())) {
            throw new InvalidDateForModeException(
                    DeliveryMode.DELIVERY_ASAP,
                    bookingDate,
                    "DELIVERY_ASAP can only be booked for today"
            );
        }

        // Max 4 hours ahead
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        Duration timeUntilBooking = Duration.between(now, bookingDateTime);

        if (timeUntilBooking.toHours() > 4) {
            throw new AsapWindowExceededException(bookingTime, now.toLocalTime());
        }
    }
}
