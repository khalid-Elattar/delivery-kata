package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.exception.*;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookingValidator")
class BookingValidatorTest {

    private BookingValidator validator;
    private UserId userId;

    @BeforeEach
    void setUp() {
        validator = new BookingValidator();
        userId = UserId.from(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Slot Template Validation")
    class SlotTemplateValidationTests {

        @Test
        @DisplayName("should pass for valid booking request")
        void shouldPassForValidBookingRequest() {
            // Given - DRIVE template: Monday-Saturday, 8am-8pm
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday 10am
            LocalDate bookingDate = LocalDate.of(2024, 1, 15); // Same Monday
            LocalTime bookingTime = LocalTime.of(14, 0); // 2pm (4 hours ahead, meets 2h minimum)

            // When/Then
            assertThatCode(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for booking in the past")
        void shouldThrowForBookingInPast() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 15, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(14, 0); // 1 hour in the past

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(SlotInPastException.class);
        }

        @Test
        @DisplayName("should throw for unavailable day of week")
        void shouldThrowForUnavailableDayOfWeek() {
            // Given - DRIVE is available Monday-Saturday (not Sunday)
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 20, 10, 0); // Saturday
            LocalDate bookingDate = LocalDate.of(2024, 1, 21); // Sunday
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }

        @Test
        @DisplayName("should throw for invalid booking time (not aligned with grid)")
        void shouldThrowForInvalidBookingTime() {
            // Given - DRIVE uses 60-minute slots starting at 8am
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(14, 30); // 2:30pm - not aligned (should be 14:00)

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidTimeForModeException.class);
        }

        @Test
        @DisplayName("should throw for time outside operating hours")
        void shouldThrowForTimeOutsideOperatingHours() {
            // Given - DRIVE operates 8am-8pm
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(21, 0); // 9pm - outside hours

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidTimeForModeException.class);
        }
    }

    @Nested
    @DisplayName("DRIVE Mode Validation")
    class DriveValidationTests {

        @Test
        @DisplayName("should pass for DRIVE on Saturday with 2h advance")
        void shouldPassForDriveOnSaturday() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 20, 10, 0); // Saturday 10am
            LocalDate bookingDate = LocalDate.of(2024, 1, 20); // Same Saturday
            LocalTime bookingTime = LocalTime.of(14, 0); // 2pm (4 hours ahead)

            // When/Then
            assertThatCode(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DRIVE on Sunday")
        void shouldThrowForDriveOnSunday() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 20, 10, 0); // Saturday
            LocalDate bookingDate = LocalDate.of(2024, 1, 21); // Sunday
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }

        @Test
        @DisplayName("should throw for DRIVE with less than 2h advance")
        void shouldThrowForDriveWithLessThan2hAdvance() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(11, 0); // Only 1 hour ahead

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }

        @Test
        @DisplayName("should throw for DRIVE booking more than 14 days ahead")
        void shouldThrowForDriveBookingTooFarAhead() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 30); // 15 days ahead
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MaxAdvanceDaysException.class);
        }
    }

    @Nested
    @DisplayName("DELIVERY Mode Validation")
    class DeliveryValidationTests {

        @Test
        @DisplayName("should pass for DELIVERY on weekday with 24h advance")
        void shouldPassForDeliveryOnWeekday() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday 10am
            LocalDate bookingDate = LocalDate.of(2024, 1, 16); // Tuesday
            LocalTime bookingTime = LocalTime.of(14, 0); // 28 hours ahead

            // When/Then
            assertThatCode(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY on Saturday")
        void shouldThrowForDeliveryOnSaturday() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday
            LocalDate bookingDate = LocalDate.of(2024, 1, 20); // Saturday
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY with less than 24h advance")
        void shouldThrowForDeliveryWithLessThan24hAdvance() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday 10am
            LocalDate bookingDate = LocalDate.of(2024, 1, 16); // Tuesday
            LocalTime bookingTime = LocalTime.of(9, 0); // Only 23 hours ahead

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY booking more than 7 days ahead")
        void shouldThrowForDeliveryBookingTooFarAhead() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 23); // 8 days ahead
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MaxAdvanceDaysException.class);
        }
    }

    @Nested
    @DisplayName("DELIVERY_TODAY Mode Validation")
    class DeliveryTodayValidationTests {

        @Test
        @DisplayName("should pass for DELIVERY_TODAY before cutoff")
        void shouldPassForDeliveryTodayBeforeCutoff() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_TODAY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 14, 0); // 2pm
            LocalDate bookingDate = LocalDate.of(2024, 1, 15); // Same day
            LocalTime bookingTime = LocalTime.of(18, 0); // 6pm (4 hours ahead, meets 3h minimum)

            // When/Then
            assertThatCode(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY_TODAY booking tomorrow")
        void shouldThrowForDeliveryTodayBookingTomorrow() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_TODAY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 16); // Tomorrow
            LocalTime bookingTime = LocalTime.of(14, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY_TODAY after cutoff time")
        void shouldThrowForDeliveryTodayAfterCutoff() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_TODAY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 19, 0); // 7pm (at cutoff)
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(21, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(CutoffTimePassedException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY_TODAY with less than 3h advance")
        void shouldThrowForDeliveryTodayWithLessThan3hAdvance() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_TODAY);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 14, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(16, 0); // Only 2 hours ahead

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }
    }

    @Nested
    @DisplayName("DELIVERY_ASAP Mode Validation")
    class DeliveryAsapValidationTests {

        @Test
        @DisplayName("should pass for DELIVERY_ASAP within 4 hour window")
        void shouldPassForDeliveryAsapWithin4HourWindow() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_ASAP);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(13, 0); // 3 hours ahead (meets 30min minimum)

            // When/Then
            assertThatCode(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY_ASAP booking tomorrow")
        void shouldThrowForDeliveryAsapBookingTomorrow() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_ASAP);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 16); // Tomorrow
            LocalTime bookingTime = LocalTime.of(11, 0);

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY_ASAP outside 4 hour window")
        void shouldThrowForDeliveryAsapOutside4HourWindow() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_ASAP);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(15, 0); // 5 hours ahead (exceeds 4h max)

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(AsapWindowExceededException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY_ASAP with less than 30min advance")
        void shouldThrowForDeliveryAsapWithLessThan30MinAdvance() {
            // Given
            Slot slot = Slot.createFromMode(DeliveryMode.DELIVERY_ASAP);
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalTime bookingTime = LocalTime.of(10, 15); // Only 15 minutes ahead

            // When/Then
            assertThatThrownBy(() -> validator.validate(slot, bookingDate, bookingTime, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }
    }

    // ========== HELPER METHODS ==========

    private Booking createBooking(SlotId slotId, UserId userId, BookingStatus status) {
        return Booking.reconstitute(
                BookingId.generate(),
                slotId,
                userId,
                status,
                Instant.now(),
                status == BookingStatus.CONFIRMED ? Instant.now() : null,
                status == BookingStatus.CANCELLED ? Instant.now() : null,
                null,
                null
        );
    }
}
