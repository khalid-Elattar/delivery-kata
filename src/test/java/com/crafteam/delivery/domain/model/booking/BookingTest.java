package com.crafteam.delivery.domain.model.booking;

import com.crafteam.delivery.domain.event.BookingCancelledEvent;
import com.crafteam.delivery.domain.event.BookingConfirmedEvent;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Booking Entity")
class BookingTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create booking with valid parameters")
        void shouldCreateBookingWithValidParameters() {
            // Given
            SlotId slotId = SlotId.generate();
            UserId userId = UserId.generate();

            // When
            Booking booking = Booking.create(slotId, userId);

            // Then
            assertThat(booking.getId()).isNotNull();
            assertThat(booking.getSlotId()).isEqualTo(slotId);
            assertThat(booking.getUserId()).isEqualTo(userId);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
            assertThat(booking.getCreatedAt()).isNotNull();
            assertThat(booking.getConfirmedAt()).isNull();
            assertThat(booking.getCancelledAt()).isNull();
        }

        @Test
        @DisplayName("should throw exception for null slot ID")
        void shouldThrowExceptionForNullSlotId() {
            // When/Then
            assertThatThrownBy(() -> Booking.create(null, UserId.generate()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Slot ID is required");
        }

        @Test
        @DisplayName("should throw exception for null user ID")
        void shouldThrowExceptionForNullUserId() {
            // When/Then
            assertThatThrownBy(() -> Booking.create(SlotId.generate(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("User ID is required");
        }
    }

    @Nested
    @DisplayName("Confirm")
    class ConfirmTests {

        @Test
        @DisplayName("should confirm pending booking")
        void shouldConfirmPendingBooking() {
            // Given
            Booking booking = createPendingBooking();

            // When
            booking.confirm();

            // Then
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(booking.isConfirmed()).isTrue();
            assertThat(booking.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("should emit BookingConfirmedEvent on confirmation")
        void shouldEmitBookingConfirmedEventOnConfirmation() {
            // Given
            Booking booking = createPendingBooking();

            // When
            booking.confirm();

            // Then
            assertThat(booking.getDomainEvents()).hasSize(1);
            assertThat(booking.getDomainEvents().get(0)).isInstanceOf(BookingConfirmedEvent.class);
            BookingConfirmedEvent event = (BookingConfirmedEvent) booking.getDomainEvents().get(0);
            assertThat(event.bookingId()).isEqualTo(booking.getId());
            assertThat(event.slotId()).isEqualTo(booking.getSlotId());
        }

        @Test
        @DisplayName("should throw exception when confirming non-pending booking")
        void shouldThrowExceptionWhenConfirmingNonPendingBooking() {
            // Given
            Booking booking = createPendingBooking();
            booking.confirm();

            // When/Then
            assertThatThrownBy(booking::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot confirm booking in status");
        }
    }

    @Nested
    @DisplayName("Cancel")
    class CancelTests {

        @Test
        @DisplayName("should cancel pending booking")
        void shouldCancelPendingBooking() {
            // Given
            Booking booking = createPendingBooking();

            // When
            booking.cancel();

            // Then
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(booking.isCancelled()).isTrue();
            assertThat(booking.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("should cancel confirmed booking")
        void shouldCancelConfirmedBooking() {
            // Given
            Booking booking = createPendingBooking();
            booking.confirm();
            booking.clearDomainEvents();

            // When
            booking.cancel();

            // Then
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("should emit BookingCancelledEvent on cancellation")
        void shouldEmitBookingCancelledEventOnCancellation() {
            // Given
            Booking booking = createPendingBooking();

            // When
            booking.cancel();

            // Then
            assertThat(booking.getDomainEvents()).hasSize(1);
            assertThat(booking.getDomainEvents().get(0)).isInstanceOf(BookingCancelledEvent.class);
            BookingCancelledEvent event = (BookingCancelledEvent) booking.getDomainEvents().get(0);
            assertThat(event.bookingId()).isEqualTo(booking.getId());
            assertThat(event.slotId()).isEqualTo(booking.getSlotId());
        }

        @Test
        @DisplayName("should throw exception when cancelling already cancelled booking")
        void shouldThrowExceptionWhenCancellingAlreadyCancelledBooking() {
            // Given
            Booking booking = createPendingBooking();
            booking.cancel();

            // When/Then
            assertThatThrownBy(booking::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already cancelled");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("should correctly identify pending status")
        void shouldCorrectlyIdentifyPendingStatus() {
            // Given
            Booking booking = createPendingBooking();

            // Then
            assertThat(booking.isPending()).isTrue();
            assertThat(booking.isConfirmed()).isFalse();
            assertThat(booking.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("should correctly identify confirmed status")
        void shouldCorrectlyIdentifyConfirmedStatus() {
            // Given
            Booking booking = createPendingBooking();
            booking.confirm();

            // Then
            assertThat(booking.isPending()).isFalse();
            assertThat(booking.isConfirmed()).isTrue();
            assertThat(booking.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("should correctly identify cancelled status")
        void shouldCorrectlyIdentifyCancelledStatus() {
            // Given
            Booking booking = createPendingBooking();
            booking.cancel();

            // Then
            assertThat(booking.isPending()).isFalse();
            assertThat(booking.isConfirmed()).isFalse();
            assertThat(booking.isCancelled()).isTrue();
        }
    }

    private Booking createPendingBooking() {
        return Booking.create(SlotId.generate(), UserId.generate());
    }
}
