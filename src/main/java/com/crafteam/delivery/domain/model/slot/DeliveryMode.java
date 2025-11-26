package com.crafteam.delivery.domain.model.slot;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

/**
 * Represents the available delivery modes.
 * Each mode has specific rules for availability and slot duration.
 */
public enum DeliveryMode {

    DRIVE(Duration.ofMinutes(60), Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)),

    DELIVERY(Duration.ofMinutes(120), Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),

    DELIVERY_TODAY(Duration.ofMinutes(60), Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),

    DELIVERY_ASAP(Duration.ofMinutes(30), Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

    private final Duration slotDuration;
    private final Set<DayOfWeek> availableDays;

    DeliveryMode(Duration slotDuration, Set<DayOfWeek> availableDays) {
        this.slotDuration = slotDuration;
        this.availableDays = availableDays;
    }

    public Duration getSlotDuration() {
        return slotDuration;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    /**
     * Checks if this delivery mode is available for the given date.
     */
    public boolean isAvailableFor(LocalDate date) {
        return availableDays.contains(date.getDayOfWeek());
    }

    /**
     * For DELIVERY_TODAY, checks if the date is today.
     * For DELIVERY_ASAP, the slot must be within the next few hours.
     */
    public boolean isValidDate(LocalDate date, LocalDate today) {
        if (!isAvailableFor(date)) {
            return false;
        }

        return switch (this) {
            case DELIVERY_TODAY -> date.equals(today);
            case DELIVERY_ASAP -> date.equals(today);
            case DRIVE, DELIVERY -> !date.isBefore(today);
        };
    }
}
