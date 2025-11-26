package com.crafteam.delivery.domain.model.slot;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Value Object representing a time slot with start and end times.
 * Immutable and self-validating.
 */
public record TimeSlot(LocalTime startTime, LocalTime endTime) {

    public TimeSlot {
        Objects.requireNonNull(startTime, "Start time is required");
        Objects.requireNonNull(endTime, "End time is required");

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time (%s) must be after start time (%s)".formatted(endTime, startTime)
            );
        }
    }

    /**
     * Creates a TimeSlot from start time and duration.
     */
    public static TimeSlot of(LocalTime startTime, Duration duration) {
        return new TimeSlot(startTime, startTime.plus(duration));
    }

    /**
     * Returns the duration of this time slot.
     */
    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Checks if the given time falls within this slot.
     */
    public boolean contains(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    /**
     * Checks if this slot overlaps with another slot.
     */
    public boolean overlaps(TimeSlot other) {
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }

    /**
     * Returns a formatted string representation (e.g., "08:00 - 10:00").
     */
    public String formatted() {
        return "%s - %s".formatted(startTime, endTime);
    }
}
