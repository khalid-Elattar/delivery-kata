package com.crafteam.delivery.domain.model.slot;

import com.crafteam.delivery.domain.event.SlotBookedEvent;
import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.exception.SlotNotAvailableException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root representing a delivery slot.
 *
 * Invariants:
 * - bookedCount <= capacity
 * - date must be valid for the delivery mode
 * - capacity must be positive
 */
public class Slot {

    private final SlotId id;
    private final DeliveryMode deliveryMode;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final int capacity;
    private int bookedCount;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor - use factory methods
    private Slot(SlotId id, DeliveryMode deliveryMode, LocalDate date,
                 TimeSlot timeSlot, int capacity, int bookedCount) {
        this.id = id;
        this.deliveryMode = deliveryMode;
        this.date = date;
        this.timeSlot = timeSlot;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
    }

    /**
     * Factory method for creating a new slot.
     */
    public static Slot create(DeliveryMode deliveryMode, LocalDate date,
                              TimeSlot timeSlot, int capacity) {
        Objects.requireNonNull(deliveryMode, "Delivery mode is required");
        Objects.requireNonNull(date, "Date is required");
        Objects.requireNonNull(timeSlot, "Time slot is required");

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        if (!deliveryMode.isAvailableFor(date)) {
            throw new IllegalArgumentException(
                    "Date %s is not available for delivery mode %s".formatted(date, deliveryMode)
            );
        }

        Slot slot = new Slot(SlotId.generate(), deliveryMode, date, timeSlot, capacity, 0);

        slot.domainEvents.add(new SlotCreatedEvent(
                slot.id, deliveryMode, date, timeSlot, capacity, Instant.now()
        ));

        return slot;
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Slot reconstitute(SlotId id, DeliveryMode deliveryMode, LocalDate date,
                                    TimeSlot timeSlot, int capacity, int bookedCount) {
        return new Slot(id, deliveryMode, date, timeSlot, capacity, bookedCount);
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Checks if the slot has available capacity.
     */
    public boolean isAvailable() {
        return bookedCount < capacity;
    }

    /**
     * Returns the remaining capacity.
     */
    public int remainingCapacity() {
        return capacity - bookedCount;
    }

    /**
     * Books this slot for a user.
     * @param userId The user making the booking
     * @return The created booking
     * @throws SlotNotAvailableException if the slot is fully booked
     */
    public Booking book(UserId userId) {
        Objects.requireNonNull(userId, "User ID is required");

        if (!isAvailable()) {
            throw new SlotNotAvailableException(
                    "Slot %s is fully booked (%d/%d)".formatted(id, bookedCount, capacity)
            );
        }

        bookedCount++;

        Booking booking = Booking.create(this.id, userId);

        domainEvents.add(new SlotBookedEvent(
                this.id,
                booking.getId(),
                userId,
                this.remainingCapacity(),
                Instant.now()
        ));

        return booking;
    }

    /**
     * Releases a booking, incrementing available capacity.
     */
    public void releaseBooking() {
        if (bookedCount <= 0) {
            throw new IllegalStateException("No bookings to release");
        }
        bookedCount--;
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

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedCount() {
        return bookedCount;
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
        return "Slot{id=%s, deliveryMode=%s, date=%s, timeSlot=%s, capacity=%d, bookedCount=%d}"
                .formatted(id, deliveryMode, date, timeSlot.formatted(), capacity, bookedCount);
    }
}
