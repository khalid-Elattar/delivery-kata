package com.crafteam.delivery.domain.model.booking;

import com.crafteam.delivery.domain.event.BookingCancelledEvent;
import com.crafteam.delivery.domain.event.BookingConfirmedEvent;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a booking for a delivery slot.
 */
public class Booking {

    private final BookingId id;
    private final SlotId slotId;
    private final CustomerId customerId;
    private BookingStatus status;
    private final Instant createdAt;
    private Instant confirmedAt;
    private Instant cancelledAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Booking(BookingId id, SlotId slotId, CustomerId customerId,
                    BookingStatus status, Instant createdAt,
                    Instant confirmedAt, Instant cancelledAt) {
        this.id = id;
        this.slotId = slotId;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    /**
     * Factory method for creating a new booking.
     */
    public static Booking create(SlotId slotId, CustomerId customerId) {
        Objects.requireNonNull(slotId, "Slot ID is required");
        Objects.requireNonNull(customerId, "Customer ID is required");

        return new Booking(
                BookingId.generate(),
                slotId,
                customerId,
                BookingStatus.PENDING,
                Instant.now(),
                null,
                null
        );
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Booking reconstitute(BookingId id, SlotId slotId, CustomerId customerId,
                                       BookingStatus status, Instant createdAt,
                                       Instant confirmedAt, Instant cancelledAt) {
        return new Booking(id, slotId, customerId, status, createdAt, confirmedAt, cancelledAt);
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Confirms this booking.
     * @throws IllegalStateException if the booking is not pending
     */
    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot confirm booking in status: " + status
            );
        }

        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = Instant.now();

        domainEvents.add(new BookingConfirmedEvent(
                this.id, this.slotId, this.customerId, Instant.now()
        ));
    }

    /**
     * Cancels this booking.
     * @throws IllegalStateException if the booking is already cancelled
     */
    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();

        domainEvents.add(new BookingCancelledEvent(
                this.id, this.slotId, 1, Instant.now()
        ));
    }

    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    // ========== EVENT HANDLING ==========

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== GETTERS ==========

    public BookingId getId() {
        return id;
    }

    public SlotId getSlotId() {
        return slotId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    // ========== EQUALITY (by ID) ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking booking)) return false;
        return id.equals(booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{id=%s, slotId=%s, customerId=%s, status=%s, createdAt=%s}"
                .formatted(id, slotId, customerId, status, createdAt);
    }
}
