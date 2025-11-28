package com.crafteam.delivery.domain.model.slot;

import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.model.shared.DomainEvent;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate Root representing a delivery slot availability template.
 *
 * This is NOT a specific booking slot for a specific date.
 * Instead, it defines WHEN a delivery mode is available (rules/template).
 *
 * Example:
 * - DRIVE is available Monday-Saturday, 08:00-20:00, 60min slots, capacity 10
 * - When a user books DRIVE for Monday 2025-12-01 at 10:00, the booking
 *   references this template and specifies the date/time
 *
 * Invariants:
 * - availableDays must not be empty
 * - startTime must be before endTime
 * - slotDuration must be positive
 * - capacity must be positive
 */
public class Slot {

    private final SlotId id;
    private final DeliveryMode deliveryMode;
    private final Set<DayOfWeek> availableDays;
    private final LocalTime startTime;      // Operating hours start (e.g., 08:00)
    private final LocalTime endTime;        // Operating hours end (e.g., 20:00)
    private final Duration slotDuration;    // Duration per booking (e.g., 60 min)
    private final int capacity;             // Max bookings per time slot per day

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor - use factory methods
    private Slot(SlotId id, DeliveryMode deliveryMode, Set<DayOfWeek> availableDays,
                 LocalTime startTime, LocalTime endTime, Duration slotDuration, int capacity) {
        this.id = id;
        this.deliveryMode = deliveryMode;
        this.availableDays = availableDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDuration = slotDuration;
        this.capacity = capacity;
    }

    /**
     * Factory method for creating a new slot template.
     */
    public static Slot create(DeliveryMode deliveryMode, Set<DayOfWeek> availableDays,
                              LocalTime startTime, LocalTime endTime,
                              Duration slotDuration, int capacity) {
        Objects.requireNonNull(deliveryMode, "Delivery mode is required");
        Objects.requireNonNull(availableDays, "Available days is required");
        Objects.requireNonNull(startTime, "Start time is required");
        Objects.requireNonNull(endTime, "End time is required");
        Objects.requireNonNull(slotDuration, "Slot duration is required");

        if (availableDays.isEmpty()) {
            throw new IllegalArgumentException("At least one available day is required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (slotDuration.isZero() || slotDuration.isNegative()) {
            throw new IllegalArgumentException("Slot duration must be positive");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        Slot slot = new Slot(SlotId.generate(), deliveryMode, availableDays,
                startTime, endTime, slotDuration, capacity);

        slot.domainEvents.add(new SlotCreatedEvent(
                slot.id, deliveryMode, availableDays, startTime, endTime, capacity, Instant.now()
        ));

        return slot;
    }

    /**
     * Convenient factory method using DeliveryMode defaults.
     */
    public static Slot createFromMode(DeliveryMode mode) {
        return create(
                mode,
                mode.getAvailableDays(),
                mode.getStartTime(),
                mode.getEndTime(),
                mode.getSlotDuration(),
                mode.getDefaultCapacity()
        );
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Slot reconstitute(SlotId id, DeliveryMode deliveryMode,
                                    Set<DayOfWeek> availableDays,
                                    LocalTime startTime, LocalTime endTime,
                                    Duration slotDuration, int capacity) {
        return new Slot(id, deliveryMode, availableDays, startTime, endTime, slotDuration, capacity);
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Check if booking is allowed on this day of week.
     */
    public boolean isAvailableOn(DayOfWeek day) {
        return availableDays.contains(day);
    }

    /**
     * Check if booking time is valid for this slot template.
     *
     * Validates:
     * 1. Time must be >= startTime
     * 2. Time must allow full duration before endTime
     * 3. Time must align with slot grid (be a multiple of slotDuration from startTime)
     */
    public boolean isValidBookingTime(LocalTime bookingTime) {
        // 1. Must be >= startTime
        if (bookingTime.isBefore(startTime)) {
            return false;
        }

        // 2. Must allow full duration before endTime
        LocalTime bookingEndTime = bookingTime.plus(slotDuration);
        if (bookingEndTime.isAfter(endTime)) {
            return false;
        }

        // 3. Must align with slot grid
        long minutesSinceStart = Duration.between(startTime, bookingTime).toMinutes();
        long slotMinutes = slotDuration.toMinutes();

        return minutesSinceStart % slotMinutes == 0;
    }

    /**
     * Get all valid booking times for this slot template.
     * Returns list of slot start times that align with the grid.
     *
     * Example: DRIVE (08:00-20:00, 60min) returns [08:00, 09:00, ..., 19:00]
     */
    public List<LocalTime> getValidBookingTimes() {
        List<LocalTime> times = new ArrayList<>();
        LocalTime current = startTime;

        while (current.plus(slotDuration).compareTo(endTime) <= 0) {
            times.add(current);
            current = current.plus(slotDuration);
        }

        return times;
    }

    /**
     * Calculate end time for a booking starting at the given time.
     */
    public LocalTime calculateEndTime(LocalTime bookingTime) {
        return bookingTime.plus(slotDuration);
    }

    /**
     * Get the duration in minutes.
     */
    public long getSlotDurationMinutes() {
        return slotDuration.toMinutes();
    }

    // ========== EVENT HANDLING ==========

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== GETTERS ==========

    public SlotId getId() {
        return id;
    }

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return Collections.unmodifiableSet(availableDays);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Duration getSlotDuration() {
        return slotDuration;
    }

    public int getCapacity() {
        return capacity;
    }

    // ========== EQUALITY (by ID) ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Slot slot)) return false;
        return id.equals(slot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Slot{id=%s, deliveryMode=%s, days=%s, time=%s-%s, duration=%dmin, capacity=%d}"
                .formatted(id, deliveryMode, availableDays, startTime, endTime,
                        slotDuration.toMinutes(), capacity);
    }
}
