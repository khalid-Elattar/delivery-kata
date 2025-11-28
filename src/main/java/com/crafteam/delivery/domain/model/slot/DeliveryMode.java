package com.crafteam.delivery.domain.model.slot;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents the available delivery modes.
 * Each mode has specific rules for availability, slot duration, and booking constraints.
 */
public enum DeliveryMode {

    DRIVE(
            Duration.ofMinutes(60),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
            LocalTime.of(8, 0),
            LocalTime.of(20, 0),
            2,      // minAdvanceHours
            14,     // maxAdvanceDays
            10,     // defaultCapacity
            null,   // cutoffTime (not applicable)
            null    // maxAdvanceHours (not applicable)
    ),

    DELIVERY(
            Duration.ofMinutes(120),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(21, 0),
            24,     // minAdvanceHours (24h = 1 day)
            7,      // maxAdvanceDays
            5,      // defaultCapacity
            null,   // cutoffTime (not applicable)
            null    // maxAdvanceHours (not applicable)
    ),

    DELIVERY_TODAY(
            Duration.ofMinutes(60),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            LocalTime.of(10, 0),
            LocalTime.of(22, 0),
            3,      // minAdvanceHours
            0,      // maxAdvanceDays (today only)
            3,      // defaultCapacity
            LocalTime.of(19, 0),  // cutoffTime
            null    // maxAdvanceHours (not applicable)
    ),

    DELIVERY_ASAP(
            Duration.ofMinutes(30),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            LocalTime.of(8, 0),
            LocalTime.of(23, 0),
            0,      // minAdvanceHours (30 minutes, handled specially)
            0,      // maxAdvanceDays (today only)
            2,      // defaultCapacity
            null,   // cutoffTime (not applicable)
            4       // maxAdvanceHours (4 hours window)
    );

    private final Duration slotDuration;
    private final Set<DayOfWeek> availableDays;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int minAdvanceHours;
    private final int maxAdvanceDays;
    private final int defaultCapacity;
    private final LocalTime cutoffTime;
    private final Integer maxAdvanceHours;

    // Minimum advance for ASAP mode (30 minutes)
    private static final Duration ASAP_MIN_ADVANCE = Duration.ofMinutes(30);

    DeliveryMode(Duration slotDuration, Set<DayOfWeek> availableDays,
                 LocalTime startTime, LocalTime endTime,
                 int minAdvanceHours, int maxAdvanceDays, int defaultCapacity,
                 LocalTime cutoffTime, Integer maxAdvanceHours) {
        this.slotDuration = slotDuration;
        this.availableDays = availableDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minAdvanceHours = minAdvanceHours;
        this.maxAdvanceDays = maxAdvanceDays;
        this.defaultCapacity = defaultCapacity;
        this.cutoffTime = cutoffTime;
        this.maxAdvanceHours = maxAdvanceHours;
    }

    // ========== GETTERS ==========

    public Duration getSlotDuration() {
        return slotDuration;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getMinAdvanceHours() {
        return minAdvanceHours;
    }

    public int getMaxAdvanceDays() {
        return maxAdvanceDays;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public LocalTime getCutoffTime() {
        return cutoffTime;
    }

    public Integer getMaxAdvanceHours() {
        return maxAdvanceHours;
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Checks if this delivery mode is available for the given day of week.
     */
    public boolean isAvailableFor(LocalDate date) {
        return availableDays.contains(date.getDayOfWeek());
    }

    /**
     * Validates if the date is valid for this delivery mode.
     * Takes into account day availability and mode-specific date constraints.
     */
    public boolean isValidDate(LocalDate date, LocalDate today) {
        if (!isAvailableFor(date)) {
            return false;
        }

        return switch (this) {
            case DELIVERY_TODAY, DELIVERY_ASAP -> date.equals(today);
            case DRIVE, DELIVERY -> !date.isBefore(today);
        };
    }

    /**
     * Validates if the slot time is within the allowed time range for this mode.
     */
    public boolean isValidSlotTime(LocalTime slotStartTime) {
        return !slotStartTime.isBefore(startTime) && !slotStartTime.isAfter(endTime.minus(slotDuration));
    }

    /**
     * Check if the given time is a valid slot start time for this mode.
     * The time must:
     * 1. Be >= startTime
     * 2. Allow a full slot before endTime (last valid start = endTime - slotDuration)
     * 3. Align with the slot grid (minutes since start must be divisible by slot duration)
     *
     * For DRIVE (1h slots, 08:00-20:00):
     *   - Valid: 08:00, 09:00, 10:00, ... 19:00
     *   - Invalid: 08:30, 10:15, 20:00
     *
     * For DELIVERY (2h slots, 09:00-21:00):
     *   - Valid: 09:00, 11:00, 13:00, 15:00, 17:00, 19:00
     *   - Invalid: 10:00, 12:00, 14:00, 20:00
     *
     * @param time The time to check
     * @return true if the time is a valid slot start time for this mode
     */
    public boolean isValidSlotStartTime(LocalTime time) {
        // 1. Time must be >= startTime
        if (time.isBefore(this.startTime)) {
            return false;
        }

        // 2. Time must allow full slot before endTime
        // Last valid start = endTime - slotDuration
        LocalTime lastValidStart = this.endTime.minus(this.slotDuration);
        if (time.isAfter(lastValidStart)) {
            return false;
        }

        // 3. Time must align with slot grid
        // Minutes since start must be divisible by slot duration
        long minutesSinceStart = Duration.between(this.startTime, time).toMinutes();
        long slotDurationMinutes = this.slotDuration.toMinutes();

        return minutesSinceStart % slotDurationMinutes == 0;
    }

    /**
     * Get all valid slot start times for this mode.
     * Returns a list of all possible slot start times that align with the slot grid.
     *
     * @return List of valid slot start times
     */
    public List<LocalTime> getAvailableSlotTimes() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = this.startTime;
        LocalTime lastValidStart = this.endTime.minus(this.slotDuration);

        while (!current.isAfter(lastValidStart)) {
            slots.add(current);
            current = current.plus(this.slotDuration);
        }
        return slots;
    }

    /**
     * Checks if booking is allowed based on minimum advance time requirement.
     * @param slotDateTime The date and time of the slot
     * @param now The current date and time
     * @return true if the minimum advance time requirement is met
     */
    public boolean meetsMinAdvanceTime(LocalDateTime slotDateTime, LocalDateTime now) {
        if (this == DELIVERY_ASAP) {
            // ASAP mode has 30 minutes minimum advance
            return now.plus(ASAP_MIN_ADVANCE).isBefore(slotDateTime) ||
                   now.plus(ASAP_MIN_ADVANCE).equals(slotDateTime);
        }

        LocalDateTime minAllowedSlotTime = now.plusHours(minAdvanceHours);
        return !slotDateTime.isBefore(minAllowedSlotTime);
    }

    /**
     * Checks if booking is allowed based on maximum advance days requirement.
     * @param slotDate The date of the slot
     * @param today Current date
     * @return true if the maximum advance days requirement is met
     */
    public boolean meetsMaxAdvanceDays(LocalDate slotDate, LocalDate today) {
        if (this == DELIVERY_ASAP) {
            // ASAP uses maxAdvanceHours instead
            return true;
        }

        long daysBetween = ChronoUnit.DAYS.between(today, slotDate);
        return daysBetween <= maxAdvanceDays;
    }

    /**
     * Checks if the booking is within the ASAP window (max 4 hours ahead).
     * Only applicable for DELIVERY_ASAP mode.
     */
    public boolean meetsAsapWindow(LocalDateTime slotDateTime, LocalDateTime now) {
        if (this != DELIVERY_ASAP) {
            return true;
        }

        LocalDateTime maxSlotTime = now.plusHours(maxAdvanceHours);
        return !slotDateTime.isAfter(maxSlotTime);
    }

    /**
     * Checks if the cutoff time has passed.
     * Only applicable for DELIVERY_TODAY mode.
     */
    public boolean isCutoffTimePassed(LocalTime currentTime) {
        if (this != DELIVERY_TODAY || cutoffTime == null) {
            return false;
        }
        return !currentTime.isBefore(cutoffTime);
    }

    /**
     * Comprehensive validation of booking timing.
     * Validates all time-related constraints for this delivery mode.
     *
     * @param slotDateTime The date and time of the slot start
     * @param now The current date and time
     * @return ValidationResult containing whether valid and reason if not
     */
    public ValidationResult validateBookingTime(LocalDateTime slotDateTime, LocalDateTime now) {
        LocalDate slotDate = slotDateTime.toLocalDate();
        LocalDate today = now.toLocalDate();
        LocalTime slotTime = slotDateTime.toLocalTime();
        LocalTime currentTime = now.toLocalTime();

        // Check day availability
        if (!isAvailableFor(slotDate)) {
            return ValidationResult.failure("INVALID_DAY",
                    "Le mode %s n'est pas disponible le %s".formatted(
                            this.name(), slotDate.getDayOfWeek()));
        }

        // Check date validity for mode
        if (!isValidDate(slotDate, today)) {
            return ValidationResult.failure("INVALID_DATE",
                    "La date %s n'est pas valide pour le mode %s".formatted(slotDate, this.name()));
        }

        // Check slot time range
        if (!isValidSlotTime(slotTime)) {
            return ValidationResult.failure("INVALID_TIME",
                    "L'heure %s n'est pas dans la plage horaire du mode %s (%s-%s)".formatted(
                            slotTime, this.name(), startTime, endTime));
        }

        // Check minimum advance time
        if (!meetsMinAdvanceTime(slotDateTime, now)) {
            String advanceDescription = this == DELIVERY_ASAP ? "30 minutes" : minAdvanceHours + " heures";
            return ValidationResult.failure("MIN_ADVANCE_TIME_NOT_MET",
                    "Le mode %s nécessite une réservation au moins %s à l'avance".formatted(
                            this.name(), advanceDescription));
        }

        // Check maximum advance days
        if (!meetsMaxAdvanceDays(slotDate, today)) {
            return ValidationResult.failure("MAX_ADVANCE_DAYS_EXCEEDED",
                    "Le mode %s ne permet pas de réserver plus de %d jours à l'avance".formatted(
                            this.name(), maxAdvanceDays));
        }

        // Check ASAP window
        if (!meetsAsapWindow(slotDateTime, now)) {
            return ValidationResult.failure("ASAP_WINDOW_EXCEEDED",
                    "Le mode DELIVERY_ASAP ne permet de réserver que dans les %d prochaines heures".formatted(
                            maxAdvanceHours));
        }

        // Check cutoff time for DELIVERY_TODAY
        if (isCutoffTimePassed(currentTime)) {
            return ValidationResult.failure("CUTOFF_TIME_PASSED",
                    "L'heure limite de commande (%s) est dépassée pour DELIVERY_TODAY".formatted(cutoffTime));
        }

        return ValidationResult.success();
    }

    /**
     * Result of a validation check.
     */
    public record ValidationResult(boolean isValid, String errorCode, String message) {
        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult failure(String errorCode, String message) {
            return new ValidationResult(false, errorCode, message);
        }

        public boolean valid() {
            return isValid;
        }
    }
}
