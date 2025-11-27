package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.exception.*;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Domain service responsible for validating booking business rules.
 * All validation rules are enforced here before a booking can be created.
 */
public class BookingValidator {

    private static final int DEFAULT_MAX_ACTIVE_BOOKINGS = 3;
    private static final int CANCELLATION_DEADLINE_HOURS = 1;

    private final int maxActiveBookings;

    public BookingValidator() {
        this(DEFAULT_MAX_ACTIVE_BOOKINGS);
    }

    public BookingValidator(int maxActiveBookings) {
        this.maxActiveBookings = maxActiveBookings;
    }

    /**
     * Validates all booking rules for a slot.
     *
     * @param slot The slot to book
     * @param userId The user making the booking
     * @param existingBookingsForSlot Existing bookings for this slot
     * @param userActiveBookings User's current active bookings
     * @param now Current date/time for validation
     * @throws BookingValidationException if any validation rule fails
     */
    public void validateBooking(Slot slot, UserId userId,
                                List<Booking> existingBookingsForSlot,
                                List<Booking> userActiveBookings,
                                LocalDateTime now) {
        // RG02: Pas de réservation dans le passé
        validateSlotNotInPast(slot, now);

        // RG03: Capacité du créneau
        validateSlotAvailable(slot);

        // Validation des règles du mode de livraison
        validateDateForMode(slot, now);
        validateTimeForMode(slot);
        validateMinAdvanceTime(slot, now);
        validateMaxAdvanceDays(slot, now);
        validateCutoffTime(slot, now);
        validateAsapWindow(slot, now);

        // RG01: Unicité de réservation
        validateUserNotAlreadyBooked(slot, userId, existingBookingsForSlot);

        // RG08: Limite de réservations actives
        validateUserActiveBookingsLimit(userId, userActiveBookings);
    }

    /**
     * RG02: Validates that the slot is not in the past.
     */
    public void validateSlotNotInPast(Slot slot, LocalDateTime now) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());
        if (slotDateTime.isBefore(now)) {
            throw new SlotInPastException(slotDateTime, now);
        }
    }

    /**
     * RG03: Validates that the slot has available capacity.
     */
    public void validateSlotAvailable(Slot slot) {
        if (!slot.isAvailable()) {
            throw new SlotNotAvailableException(
                    "Créneau complet (%d/%d)".formatted(slot.getBookedCount(), slot.getCapacity())
            );
        }
    }

    /**
     * RG04 & RG06: Validates that the date is valid for the delivery mode.
     */
    public void validateDateForMode(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();
        LocalDate slotDate = slot.getDate();
        LocalDate today = now.toLocalDate();

        // Check day of week availability
        if (!mode.isAvailableFor(slotDate)) {
            throw new InvalidDateForModeException(mode, slotDate);
        }

        // Check mode-specific date rules
        if (!mode.isValidDate(slotDate, today)) {
            String reason = switch (mode) {
                case DELIVERY_TODAY, DELIVERY_ASAP ->
                        "Le mode %s ne permet de réserver que pour aujourd'hui".formatted(mode.name());
                case DRIVE, DELIVERY ->
                        "La date ne peut pas être dans le passé";
            };
            throw new InvalidDateForModeException(mode, slotDate, reason);
        }
    }

    /**
     * RG05: Validates that the slot time is within allowed range for the mode.
     */
    public void validateTimeForMode(Slot slot) {
        DeliveryMode mode = slot.getDeliveryMode();
        LocalTime slotTime = slot.getTimeSlot().startTime();

        if (!mode.isValidSlotTime(slotTime)) {
            throw new InvalidTimeForModeException(mode, slotTime);
        }
    }

    /**
     * Validates minimum advance time requirement.
     */
    public void validateMinAdvanceTime(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());

        if (!mode.meetsMinAdvanceTime(slotDateTime, now)) {
            throw new MinAdvanceTimeException(mode, slotDateTime, now);
        }
    }

    /**
     * Validates maximum advance days requirement.
     */
    public void validateMaxAdvanceDays(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();
        LocalDate slotDate = slot.getDate();
        LocalDate today = now.toLocalDate();

        if (!mode.meetsMaxAdvanceDays(slotDate, today)) {
            throw new MaxAdvanceDaysException(mode, slotDate, today);
        }
    }

    /**
     * Validates cutoff time for DELIVERY_TODAY mode.
     */
    public void validateCutoffTime(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();

        if (mode == DeliveryMode.DELIVERY_TODAY && mode.isCutoffTimePassed(now.toLocalTime())) {
            throw new CutoffTimePassedException(mode.getCutoffTime(), now.toLocalTime());
        }
    }

    /**
     * Validates ASAP window (4 hours max) for DELIVERY_ASAP mode.
     */
    public void validateAsapWindow(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();

        if (mode == DeliveryMode.DELIVERY_ASAP) {
            LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());

            if (!mode.meetsAsapWindow(slotDateTime, now)) {
                throw new AsapWindowExceededException(slotDateTime, now, mode.getMaxAdvanceHours());
            }
        }
    }

    /**
     * RG01: Validates that the user doesn't already have a booking for this slot.
     */
    public void validateUserNotAlreadyBooked(Slot slot, UserId userId, List<Booking> existingBookingsForSlot) {
        boolean alreadyBooked = existingBookingsForSlot.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .anyMatch(b -> b.getUserId().equals(userId));

        if (alreadyBooked) {
            throw new UserAlreadyBookedException(userId, slot.getId());
        }
    }

    /**
     * RG08: Validates that the user doesn't exceed the maximum active bookings limit.
     */
    public void validateUserActiveBookingsLimit(UserId userId, List<Booking> userActiveBookings) {
        long activeCount = userActiveBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();

        if (activeCount >= maxActiveBookings) {
            throw new MaxActiveBookingsException(userId, (int) activeCount, maxActiveBookings);
        }
    }

    /**
     * RG07: Validates that cancellation is allowed (at least 1 hour before slot start).
     */
    public void validateCancellationAllowed(Booking booking, LocalDateTime slotStartTime, LocalDateTime now) {
        LocalDateTime cancellationDeadline = slotStartTime.minusHours(CANCELLATION_DEADLINE_HOURS);

        if (now.isAfter(cancellationDeadline)) {
            throw new CancellationNotAllowedException(booking.getId(), slotStartTime, now);
        }
    }

    public int getMaxActiveBookings() {
        return maxActiveBookings;
    }
}
