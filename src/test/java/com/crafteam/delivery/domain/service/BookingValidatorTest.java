package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.exception.*;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import com.crafteam.delivery.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookingValidator")
class BookingValidatorTest {

    private BookingValidator validator;
    private UserId userId;

    @BeforeEach
    void setUp() {
        validator = new BookingValidator(3);
        userId = UserId.from(UUID.randomUUID());
    }

    @Nested
    @DisplayName("validateSlotNotInPast")
    class ValidateSlotNotInPastTests {

        @Test
        @DisplayName("should pass for slot in future")
        void shouldPassForSlotInFuture() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(14, 0));

            assertThatCode(() -> validator.validateSlotNotInPast(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for slot in past")
        void shouldThrowForSlotInPast() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 15, 0);
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(14, 0));

            assertThatThrownBy(() -> validator.validateSlotNotInPast(slot, now))
                    .isInstanceOf(SlotInPastException.class);
        }
    }

    @Nested
    @DisplayName("validateSlotAvailable")
    class ValidateSlotAvailableTests {

        @Test
        @DisplayName("should pass for available slot")
        void shouldPassForAvailableSlot() {
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(10, 0));

            assertThatCode(() -> validator.validateSlotAvailable(slot))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for fully booked slot")
        void shouldThrowForFullyBookedSlot() {
            Slot slot = createFullyBookedSlot();

            assertThatThrownBy(() -> validator.validateSlotAvailable(slot))
                    .isInstanceOf(SlotNotAvailableException.class);
        }
    }

    @Nested
    @DisplayName("validateDateForMode - DRIVE")
    class ValidateDateForModeDriveTests {

        @Test
        @DisplayName("should pass for DRIVE on Saturday")
        void shouldPassForDriveOnSaturday() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 20, 10, 0); // Saturday
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 20), LocalTime.of(14, 0));

            assertThatCode(() -> validator.validateDateForMode(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DRIVE on Sunday")
        void shouldThrowForDriveOnSunday() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 21, 10, 0); // Sunday
            Slot slot = createSlotWithDate(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 21));

            assertThatThrownBy(() -> validator.validateDateForMode(slot, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }
    }

    @Nested
    @DisplayName("validateDateForMode - DELIVERY")
    class ValidateDateForModeDeliveryTests {

        @Test
        @DisplayName("should pass for DELIVERY on Monday")
        void shouldPassForDeliveryOnMonday() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 14, 10, 0); // Sunday
            LocalDate monday = LocalDate.of(2024, 1, 15); // Monday
            Slot slot = createSlot(DeliveryMode.DELIVERY, monday, LocalTime.of(10, 0));

            assertThatCode(() -> validator.validateDateForMode(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY on Saturday")
        void shouldThrowForDeliveryOnSaturday() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 20, 10, 0); // Saturday
            Slot slot = createSlotWithDate(DeliveryMode.DELIVERY, LocalDate.of(2024, 1, 20));

            assertThatThrownBy(() -> validator.validateDateForMode(slot, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }
    }

    @Nested
    @DisplayName("validateDateForMode - DELIVERY_TODAY")
    class ValidateDateForModeDeliveryTodayTests {

        @Test
        @DisplayName("should pass for DELIVERY_TODAY booking today")
        void shouldPassForDeliveryTodayBookingToday() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY_TODAY, LocalDate.of(2024, 1, 15), LocalTime.of(14, 0));

            assertThatCode(() -> validator.validateDateForMode(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY_TODAY booking tomorrow")
        void shouldThrowForDeliveryTodayBookingTomorrow() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlotWithDate(DeliveryMode.DELIVERY_TODAY, LocalDate.of(2024, 1, 16));

            assertThatThrownBy(() -> validator.validateDateForMode(slot, now))
                    .isInstanceOf(InvalidDateForModeException.class);
        }
    }

    @Nested
    @DisplayName("validateMinAdvanceTime")
    class ValidateMinAdvanceTimeTests {

        @Test
        @DisplayName("should pass for DRIVE with 3 hours advance")
        void shouldPassForDriveWith3HoursAdvance() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(13, 0));

            assertThatCode(() -> validator.validateMinAdvanceTime(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DRIVE with 1 hour advance")
        void shouldThrowForDriveWith1HourAdvance() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(11, 0));

            assertThatThrownBy(() -> validator.validateMinAdvanceTime(slot, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY with less than 24 hours advance")
        void shouldThrowForDeliveryWithLessThan24HoursAdvance() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY, LocalDate.of(2024, 1, 16), LocalTime.of(9, 0));

            assertThatThrownBy(() -> validator.validateMinAdvanceTime(slot, now))
                    .isInstanceOf(MinAdvanceTimeException.class);
        }
    }

    @Nested
    @DisplayName("validateMaxAdvanceDays")
    class ValidateMaxAdvanceDaysTests {

        @Test
        @DisplayName("should pass for DRIVE booking 14 days ahead")
        void shouldPassForDriveBooking14DaysAhead() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate slotDate = LocalDate.of(2024, 1, 29);
            Slot slot = createSlot(DeliveryMode.DRIVE, slotDate, LocalTime.of(10, 0));

            assertThatCode(() -> validator.validateMaxAdvanceDays(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DRIVE booking 15 days ahead")
        void shouldThrowForDriveBooking15DaysAhead() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate slotDate = LocalDate.of(2024, 1, 30);
            Slot slot = createSlotWithDate(DeliveryMode.DRIVE, slotDate);

            assertThatThrownBy(() -> validator.validateMaxAdvanceDays(slot, now))
                    .isInstanceOf(MaxAdvanceDaysException.class);
        }

        @Test
        @DisplayName("should throw for DELIVERY booking 8 days ahead")
        void shouldThrowForDeliveryBooking8DaysAhead() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate slotDate = LocalDate.of(2024, 1, 23);
            Slot slot = createSlotWithDate(DeliveryMode.DELIVERY, slotDate);

            assertThatThrownBy(() -> validator.validateMaxAdvanceDays(slot, now))
                    .isInstanceOf(MaxAdvanceDaysException.class);
        }
    }

    @Nested
    @DisplayName("validateCutoffTime")
    class ValidateCutoffTimeTests {

        @Test
        @DisplayName("should pass for DELIVERY_TODAY before cutoff")
        void shouldPassForDeliveryTodayBeforeCutoff() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 18, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY_TODAY, LocalDate.of(2024, 1, 15), LocalTime.of(21, 0));

            assertThatCode(() -> validator.validateCutoffTime(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY_TODAY after cutoff")
        void shouldThrowForDeliveryTodayAfterCutoff() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 20, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY_TODAY, LocalDate.of(2024, 1, 15), LocalTime.of(21, 0));

            assertThatThrownBy(() -> validator.validateCutoffTime(slot, now))
                    .isInstanceOf(CutoffTimePassedException.class);
        }
    }

    @Nested
    @DisplayName("validateAsapWindow")
    class ValidateAsapWindowTests {

        @Test
        @DisplayName("should pass for DELIVERY_ASAP within 4 hour window")
        void shouldPassForDeliveryAsapWithin4HourWindow() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY_ASAP, LocalDate.of(2024, 1, 15), LocalTime.of(13, 0));

            assertThatCode(() -> validator.validateAsapWindow(slot, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw for DELIVERY_ASAP outside 4 hour window")
        void shouldThrowForDeliveryAsapOutside4HourWindow() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            Slot slot = createSlot(DeliveryMode.DELIVERY_ASAP, LocalDate.of(2024, 1, 15), LocalTime.of(15, 0));

            assertThatThrownBy(() -> validator.validateAsapWindow(slot, now))
                    .isInstanceOf(AsapWindowExceededException.class);
        }
    }

    @Nested
    @DisplayName("validateUserNotAlreadyBooked")
    class ValidateUserNotAlreadyBookedTests {

        @Test
        @DisplayName("should pass when user has no booking for slot")
        void shouldPassWhenUserHasNoBookingForSlot() {
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(10, 0));
            List<Booking> existingBookings = Collections.emptyList();

            assertThatCode(() -> validator.validateUserNotAlreadyBooked(slot, userId, existingBookings))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when user already has booking for slot")
        void shouldThrowWhenUserAlreadyHasBookingForSlot() {
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(10, 0));
            Booking existingBooking = createBooking(slot.getId(), userId, BookingStatus.PENDING);
            List<Booking> existingBookings = List.of(existingBooking);

            assertThatThrownBy(() -> validator.validateUserNotAlreadyBooked(slot, userId, existingBookings))
                    .isInstanceOf(UserAlreadyBookedException.class);
        }

        @Test
        @DisplayName("should pass when user's existing booking is cancelled")
        void shouldPassWhenUserExistingBookingIsCancelled() {
            Slot slot = createSlot(DeliveryMode.DRIVE, LocalDate.of(2024, 1, 15), LocalTime.of(10, 0));
            Booking cancelledBooking = createBooking(slot.getId(), userId, BookingStatus.CANCELLED);
            List<Booking> existingBookings = List.of(cancelledBooking);

            assertThatCode(() -> validator.validateUserNotAlreadyBooked(slot, userId, existingBookings))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateUserActiveBookingsLimit")
    class ValidateUserActiveBookingsLimitTests {

        @Test
        @DisplayName("should pass when user has less than 3 active bookings")
        void shouldPassWhenUserHasLessThan3ActiveBookings() {
            List<Booking> activeBookings = List.of(
                    createBooking(SlotId.generate(), userId, BookingStatus.PENDING),
                    createBooking(SlotId.generate(), userId, BookingStatus.CONFIRMED)
            );

            assertThatCode(() -> validator.validateUserActiveBookingsLimit(userId, activeBookings))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when user has 3 or more active bookings")
        void shouldThrowWhenUserHas3OrMoreActiveBookings() {
            List<Booking> activeBookings = List.of(
                    createBooking(SlotId.generate(), userId, BookingStatus.PENDING),
                    createBooking(SlotId.generate(), userId, BookingStatus.CONFIRMED),
                    createBooking(SlotId.generate(), userId, BookingStatus.PENDING)
            );

            assertThatThrownBy(() -> validator.validateUserActiveBookingsLimit(userId, activeBookings))
                    .isInstanceOf(MaxActiveBookingsException.class);
        }
    }

    @Nested
    @DisplayName("validateCancellationAllowed")
    class ValidateCancellationAllowedTests {

        @Test
        @DisplayName("should pass when cancelling 2 hours before slot")
        void shouldPassWhenCancelling2HoursBeforeSlot() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 12, 0);
            LocalDateTime slotStartTime = LocalDateTime.of(2024, 1, 15, 14, 0);
            Booking booking = createBooking(SlotId.generate(), userId, BookingStatus.CONFIRMED);

            assertThatCode(() -> validator.validateCancellationAllowed(booking, slotStartTime, now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when cancelling 30 minutes before slot")
        void shouldThrowWhenCancelling30MinutesBeforeSlot() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 13, 30);
            LocalDateTime slotStartTime = LocalDateTime.of(2024, 1, 15, 14, 0);
            Booking booking = createBooking(SlotId.generate(), userId, BookingStatus.CONFIRMED);

            assertThatThrownBy(() -> validator.validateCancellationAllowed(booking, slotStartTime, now))
                    .isInstanceOf(CancellationNotAllowedException.class);
        }
    }

    // ========== HELPER METHODS ==========

    private Slot createSlot(DeliveryMode mode, LocalDate date, LocalTime startTime) {
        TimeSlot timeSlot = TimeSlot.of(startTime, mode.getSlotDuration());
        return Slot.reconstitute(SlotId.generate(), mode, date, timeSlot, mode.getDefaultCapacity(), 0);
    }

    private Slot createSlotWithDate(DeliveryMode mode, LocalDate date) {
        TimeSlot timeSlot = TimeSlot.of(LocalTime.of(10, 0), mode.getSlotDuration());
        return Slot.reconstitute(SlotId.generate(), mode, date, timeSlot, mode.getDefaultCapacity(), 0);
    }

    private Slot createFullyBookedSlot() {
        TimeSlot timeSlot = TimeSlot.of(LocalTime.of(10, 0), DeliveryMode.DRIVE.getSlotDuration());
        int capacity = 10;
        return Slot.reconstitute(SlotId.generate(), DeliveryMode.DRIVE,
                LocalDate.of(2024, 1, 15), timeSlot, capacity, capacity);
    }

    private Booking createBooking(SlotId slotId, UserId userId, BookingStatus status) {
        return Booking.reconstitute(
                BookingId.generate(),
                slotId,
                userId,
                status,
                Instant.now(),
                status == BookingStatus.CONFIRMED ? Instant.now() : null,
                status == BookingStatus.CANCELLED ? Instant.now() : null
        );
    }
}
