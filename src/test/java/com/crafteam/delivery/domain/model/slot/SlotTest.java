package com.crafteam.delivery.domain.model.slot;

import com.crafteam.delivery.domain.event.SlotBookedEvent;
import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.exception.SlotNotAvailableException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Slot Aggregate")
class SlotTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create slot with valid parameters")
        void shouldCreateSlotWithValidParameters() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            LocalDate date = getNextValidDate(mode);
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
            int capacity = 5;

            // When
            Slot slot = Slot.create(mode, date, timeSlot, capacity);

            // Then
            assertThat(slot.getId()).isNotNull();
            assertThat(slot.getDeliveryMode()).isEqualTo(mode);
            assertThat(slot.getDate()).isEqualTo(date);
            assertThat(slot.getTimeSlot()).isEqualTo(timeSlot);
            assertThat(slot.getCapacity()).isEqualTo(capacity);
            assertThat(slot.getBookedCount()).isZero();
            assertThat(slot.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("should emit SlotCreatedEvent on creation")
        void shouldEmitSlotCreatedEventOnCreation() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            LocalDate date = getNextValidDate(mode);
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));

            // When
            Slot slot = Slot.create(mode, date, timeSlot, 5);

            // Then
            assertThat(slot.getDomainEvents()).hasSize(1);
            assertThat(slot.getDomainEvents().get(0)).isInstanceOf(SlotCreatedEvent.class);
            SlotCreatedEvent event = (SlotCreatedEvent) slot.getDomainEvents().get(0);
            assertThat(event.slotId()).isEqualTo(slot.getId());
            assertThat(event.deliveryMode()).isEqualTo(mode);
        }

        @Test
        @DisplayName("should throw exception for invalid capacity")
        void shouldThrowExceptionForInvalidCapacity() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            LocalDate date = getNextValidDate(mode);
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));

            // When/Then
            assertThatThrownBy(() -> Slot.create(mode, date, timeSlot, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Capacity must be positive");
        }

        @Test
        @DisplayName("should throw exception for date not available for delivery mode")
        void shouldThrowExceptionForInvalidDate() {
            // Given - Sunday is not available for DELIVERY mode
            DeliveryMode mode = DeliveryMode.DELIVERY;
            LocalDate sunday = getNextSunday();
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 0));

            // When/Then
            assertThatThrownBy(() -> Slot.create(mode, sunday, timeSlot, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("is not available for delivery mode");
        }
    }

    @Nested
    @DisplayName("Booking")
    class BookingTests {

        @Test
        @DisplayName("should book slot successfully")
        void shouldBookSlotSuccessfully() {
            // Given
            Slot slot = createValidSlot(5);
            UserId userId = UserId.generate();
            slot.clearDomainEvents(); // Clear creation event

            // When
            Booking booking = slot.book(userId);

            // Then
            assertThat(booking).isNotNull();
            assertThat(booking.getSlotId()).isEqualTo(slot.getId());
            assertThat(booking.getUserId()).isEqualTo(userId);
            assertThat(slot.getBookedCount()).isEqualTo(1);
            assertThat(slot.remainingCapacity()).isEqualTo(4);
        }

        @Test
        @DisplayName("should emit SlotBookedEvent on booking")
        void shouldEmitSlotBookedEventOnBooking() {
            // Given
            Slot slot = createValidSlot(5);
            UserId userId = UserId.generate();
            slot.clearDomainEvents();

            // When
            Booking booking = slot.book(userId);

            // Then
            assertThat(slot.getDomainEvents()).hasSize(1);
            assertThat(slot.getDomainEvents().get(0)).isInstanceOf(SlotBookedEvent.class);
            SlotBookedEvent event = (SlotBookedEvent) slot.getDomainEvents().get(0);
            assertThat(event.slotId()).isEqualTo(slot.getId());
            assertThat(event.bookingId()).isEqualTo(booking.getId());
            assertThat(event.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should throw exception when slot is fully booked")
        void shouldThrowExceptionWhenSlotIsFullyBooked() {
            // Given
            Slot slot = createValidSlot(1);
            slot.book(UserId.generate());

            // When/Then
            assertThatThrownBy(() -> slot.book(UserId.generate()))
                    .isInstanceOf(SlotNotAvailableException.class)
                    .hasMessageContaining("is fully booked");
        }

        @Test
        @DisplayName("should allow booking until capacity is reached")
        void shouldAllowBookingUntilCapacityIsReached() {
            // Given
            Slot slot = createValidSlot(3);

            // When
            slot.book(UserId.generate());
            slot.book(UserId.generate());
            slot.book(UserId.generate());

            // Then
            assertThat(slot.isAvailable()).isFalse();
            assertThat(slot.remainingCapacity()).isZero();
        }
    }

    @Nested
    @DisplayName("Release Booking")
    class ReleaseBookingTests {

        @Test
        @DisplayName("should release booking and increase capacity")
        void shouldReleaseBookingAndIncreaseCapacity() {
            // Given
            Slot slot = createValidSlot(2);
            slot.book(UserId.generate());
            int bookedBefore = slot.getBookedCount();

            // When
            slot.releaseBooking();

            // Then
            assertThat(slot.getBookedCount()).isEqualTo(bookedBefore - 1);
            assertThat(slot.remainingCapacity()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw exception when no bookings to release")
        void shouldThrowExceptionWhenNoBookingsToRelease() {
            // Given
            Slot slot = createValidSlot(5);

            // When/Then
            assertThatThrownBy(slot::releaseBooking)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No bookings to release");
        }
    }

    // Helper methods
    private Slot createValidSlot(int capacity) {
        DeliveryMode mode = DeliveryMode.DRIVE;
        LocalDate date = getNextValidDate(mode);
        TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
        return Slot.create(mode, date, timeSlot, capacity);
    }

    private LocalDate getNextValidDate(DeliveryMode mode) {
        LocalDate date = LocalDate.now();
        while (!mode.isAvailableFor(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private LocalDate getNextSunday() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
