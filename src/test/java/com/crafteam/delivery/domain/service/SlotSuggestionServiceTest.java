package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.*;
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

import static org.assertj.core.api.Assertions.*;

@DisplayName("SlotSuggestionService")
class SlotSuggestionServiceTest {

    private SlotSuggestionService suggestionService;
    private UserId userId;

    @BeforeEach
    void setUp() {
        suggestionService = new SlotSuggestionService(3);
        userId = UserId.generate();
    }

    @Nested
    @DisplayName("DRIVE Mode Suggestions")
    class DriveModeTests {

        @Test
        @DisplayName("Test 1: DRIVE - Should only return valid suggestions respecting all rules")
        void shouldOnlyReturnValidDriveSuggestions() {
            // Given: Saturday Jan 13, 2024 at 07:00 (Saturday)
            LocalDateTime now = LocalDateTime.of(2024, 1, 13, 7, 0); // Saturday
            LocalDate saturday = LocalDate.of(2024, 1, 13);
            LocalDate sunday = LocalDate.of(2024, 1, 14);
            LocalDate monday = LocalDate.of(2024, 1, 15);

            // Requested slot: Saturday 10h (FULL)
            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            // Candidate slots:
            List<Slot> candidates = List.of(
                    // Saturday 08:30h - available but < 2h before from 07:00 (invalid, needs to be >= 09:00)
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(8, 30), 10, 5),
                    // Saturday 11h - available, > 2h (valid)
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5),
                    // Saturday 14h - available, > 2h (valid)
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(14, 0), 10, 3),
                    // Sunday 10h - available but Sunday not available for DRIVE (invalid)
                    createSlot(DeliveryMode.DRIVE, sunday, LocalTime.of(10, 0), 10, 8),
                    // Monday 10h - available (bookedCount=5 < capacity=10), valid day (valid)
                    createSlot(DeliveryMode.DRIVE, monday, LocalTime.of(10, 0), 10, 5)
            );

            // When
            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Then: Only 3 valid suggestions (Saturday 11h, Saturday 14h, Monday 10h)
            assertThat(suggestions).hasSize(3);

            // Verify Saturday 08:30 is excluded (less than 2h advance from 07:00)
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getTimeSlot().startTime().equals(LocalTime.of(8, 30)));

            // Verify Sunday is excluded
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getDate().equals(sunday));

            // Verify valid suggestions are present
            assertThat(suggestions)
                    .anyMatch(s -> s.slot().getDate().equals(saturday) &&
                            s.slot().getTimeSlot().startTime().equals(LocalTime.of(11, 0)));
            assertThat(suggestions)
                    .anyMatch(s -> s.slot().getDate().equals(saturday) &&
                            s.slot().getTimeSlot().startTime().equals(LocalTime.of(14, 0)));
            assertThat(suggestions)
                    .anyMatch(s -> s.slot().getDate().equals(monday));
        }

        @Test
        @DisplayName("DRIVE suggestions should be sorted by proximity score")
        void driveSuggestionsShouldBeSortedByProximityScore() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 7, 0); // Saturday
            LocalDate saturday = LocalDate.of(2024, 1, 15);
            LocalDate monday = LocalDate.of(2024, 1, 17);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            List<Slot> candidates = List.of(
                    // Monday - further away
                    createSlot(DeliveryMode.DRIVE, monday, LocalTime.of(10, 0), 10, 10),
                    // Saturday 14h - same day, later
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(14, 0), 10, 3),
                    // Saturday 11h - same day, closer time
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Same day slots should come first, sorted by time proximity
            assertThat(suggestions.get(0).slot().getDate()).isEqualTo(saturday);
            assertThat(suggestions.get(0).slot().getTimeSlot().startTime()).isEqualTo(LocalTime.of(11, 0));
        }
    }

    @Nested
    @DisplayName("DELIVERY Mode Suggestions")
    class DeliveryModeTests {

        @Test
        @DisplayName("Test 2: DELIVERY - No weekend suggestions")
        void shouldExcludeWeekendForDeliverySuggestions() {
            // Given: Wednesday Jan 17, 2024 at 10:00 (need 24h before Friday)
            LocalDateTime now = LocalDateTime.of(2024, 1, 17, 10, 0);
            LocalDate friday = LocalDate.of(2024, 1, 19);
            LocalDate saturday = LocalDate.of(2024, 1, 20);
            LocalDate sunday = LocalDate.of(2024, 1, 21);
            LocalDate monday = LocalDate.of(2024, 1, 22);

            // Requested slot: Friday 14h (FULL)
            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY, friday, LocalTime.of(14, 0), 5, 5);

            List<Slot> candidates = List.of(
                    // Friday 16h - available (valid, > 24h from Wednesday 10:00)
                    createSlot(DeliveryMode.DELIVERY, friday, LocalTime.of(16, 0), 5, 2),
                    // Saturday 14h - invalid (weekend)
                    createSlot(DeliveryMode.DELIVERY, saturday, LocalTime.of(14, 0), 5, 3),
                    // Sunday 14h - invalid (weekend)
                    createSlot(DeliveryMode.DELIVERY, sunday, LocalTime.of(14, 0), 5, 4),
                    // Monday 14h - valid (weekday, > 24h) - bookedCount=3, capacity=5 = available
                    createSlot(DeliveryMode.DELIVERY, monday, LocalTime.of(14, 0), 5, 3)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Then: Only Friday and Monday (weekdays)
            assertThat(suggestions).hasSize(2);
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getDate().equals(saturday) || s.slot().getDate().equals(sunday));
            assertThat(suggestions)
                    .anyMatch(s -> s.slot().getDate().equals(friday));
            assertThat(suggestions)
                    .anyMatch(s -> s.slot().getDate().equals(monday));
        }

        @Test
        @DisplayName("DELIVERY should respect 24h minimum advance time")
        void deliveryShouldRespect24hMinAdvance() {
            // Given: Thursday Jan 18, 2024 at 10:00
            LocalDateTime now = LocalDateTime.of(2024, 1, 18, 10, 0);
            LocalDate thursday = LocalDate.of(2024, 1, 18);
            LocalDate friday = LocalDate.of(2024, 1, 19);

            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY, friday, LocalTime.of(14, 0), 5, 5);

            List<Slot> candidates = List.of(
                    // Thursday 15h - less than 24h ahead (invalid)
                    createSlot(DeliveryMode.DELIVERY, thursday, LocalTime.of(15, 0), 5, 2),
                    // Friday 9h - less than 24h ahead (invalid, < 24h from now)
                    createSlot(DeliveryMode.DELIVERY, friday, LocalTime.of(9, 0), 5, 2),
                    // Friday 11h - more than 24h ahead (valid)
                    createSlot(DeliveryMode.DELIVERY, friday, LocalTime.of(11, 0), 5, 3)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Only Friday 11h should be valid
            assertThat(suggestions).hasSize(1);
            assertThat(suggestions.get(0).slot().getTimeSlot().startTime()).isEqualTo(LocalTime.of(11, 0));
        }
    }

    @Nested
    @DisplayName("DELIVERY_TODAY Mode Suggestions")
    class DeliveryTodayModeTests {

        @Test
        @DisplayName("Test 3: DELIVERY_TODAY - Only today suggestions")
        void shouldOnlyReturnTodaySuggestionsForDeliveryToday() {
            // Given: Jan 15, 2024 at 10:00
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate today = LocalDate.of(2024, 1, 15);
            LocalDate tomorrow = LocalDate.of(2024, 1, 16);

            // Requested slot: today 14h (FULL)
            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(14, 0), 3, 3);

            List<Slot> candidates = List.of(
                    // Today 12h - less than 3h ahead (invalid - 12h is only 2h from 10h)
                    createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(12, 0), 3, 1),
                    // Today 13:30 - more than 3h ahead (valid - 13:30 is 3.5h from 10h)
                    createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(13, 30), 3, 2),
                    // Tomorrow 14h - invalid (not today)
                    createSlot(DeliveryMode.DELIVERY_TODAY, tomorrow, LocalTime.of(14, 0), 3, 3)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Then: Only today's valid slots (13:30)
            assertThat(suggestions).hasSize(1);
            assertThat(suggestions).allMatch(s -> s.slot().getDate().equals(today));
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getTimeSlot().startTime().equals(LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("Test 4: DELIVERY_TODAY - After cutoff 19h returns empty")
        void shouldReturnEmptyAfterCutoffTime() {
            // Given: Jan 15, 2024 at 19:30 (after cutoff)
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 19, 30);
            LocalDate today = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(20, 0), 3, 3);

            List<Slot> candidates = List.of(
                    createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(21, 0), 3, 2),
                    createSlot(DeliveryMode.DELIVERY_TODAY, today, LocalTime.of(22, 0), 3, 3)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Then: No suggestions (cutoff passed)
            assertThat(suggestions).isEmpty();
        }
    }

    @Nested
    @DisplayName("DELIVERY_ASAP Mode Suggestions")
    class DeliveryAsapModeTests {

        @Test
        @DisplayName("Test 5: DELIVERY_ASAP - 4h window max")
        void shouldOnlyReturn4hWindowSuggestionsForAsap() {
            // Given: Jan 15, 2024 at 10:00
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate today = LocalDate.of(2024, 1, 15);
            LocalDate tomorrow = LocalDate.of(2024, 1, 16);

            // Requested slot: today 11h (FULL)
            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(11, 0), 2, 2);

            List<Slot> candidates = List.of(
                    // Today 10:30 - valid (within 30min advance and 4h window)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(10, 30), 2, 1),
                    // Today 11:30 - valid (within 4h window)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(11, 30), 2, 1),
                    // Today 13:00 - valid (within 4h window - 3h from now)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(13, 0), 2, 1),
                    // Today 14:30 - invalid (4.5h ahead, outside 4h window)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(14, 30), 2, 2),
                    // Tomorrow 11:00 - invalid (not today)
                    createSlot(DeliveryMode.DELIVERY_ASAP, tomorrow, LocalTime.of(11, 0), 2, 2)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // Then: Only slots within 4h window (10:30, 11:30, 13:00)
            assertThat(suggestions).hasSize(3);
            assertThat(suggestions)
                    .allMatch(s -> s.slot().getDate().equals(today));
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getTimeSlot().startTime().equals(LocalTime.of(14, 30)));
        }

        @Test
        @DisplayName("DELIVERY_ASAP should respect 30min minimum advance")
        void asapShouldRespect30minMinAdvance() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate today = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(11, 0), 2, 2);

            List<Slot> candidates = List.of(
                    // Today 10:15 - invalid (less than 30min)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(10, 15), 2, 1),
                    // Today 10:30 - valid (exactly 30min)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(10, 30), 2, 1),
                    // Today 11:00 - valid (1h ahead)
                    createSlot(DeliveryMode.DELIVERY_ASAP, today, LocalTime.of(11, 0), 2, 1)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            // 10:15 should be excluded
            assertThat(suggestions).hasSize(2);
            assertThat(suggestions)
                    .noneMatch(s -> s.slot().getTimeSlot().startTime().equals(LocalTime.of(10, 15)));
        }
    }

    @Nested
    @DisplayName("User Booking Validation in Suggestions")
    class UserBookingValidationTests {

        @Test
        @DisplayName("Test 6: User with 3 active bookings gets empty suggestions")
        void shouldReturnEmptyWhenUserHasMaxBookings() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            List<Slot> candidates = List.of(
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(12, 0), 10, 5)
            );

            // User has 3 active bookings (max)
            List<Booking> userBookings = List.of(
                    createBooking(SlotId.generate(), userId, BookingStatus.PENDING),
                    createBooking(SlotId.generate(), userId, BookingStatus.CONFIRMED),
                    createBooking(SlotId.generate(), userId, BookingStatus.PENDING)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, userBookings, now
            );

            assertThat(suggestions).isEmpty();
        }

        @Test
        @DisplayName("Test 7: Slots already booked by user are excluded")
        void shouldExcludeSlotsAlreadyBookedByUser() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            SlotId alreadyBookedSlotId = SlotId.generate();
            Slot alreadyBookedSlot = createSlotWithId(alreadyBookedSlotId, DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5);
            Slot availableSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(12, 0), 10, 5);

            List<Slot> candidates = List.of(alreadyBookedSlot, availableSlot);

            // User has booked the 11h slot
            List<Booking> userBookings = List.of(
                    createBooking(alreadyBookedSlotId, userId, BookingStatus.PENDING)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, userBookings, now
            );

            // Only 12h slot should be suggested
            assertThat(suggestions).hasSize(1);
            assertThat(suggestions.get(0).slot().getTimeSlot().startTime()).isEqualTo(LocalTime.of(12, 0));
        }

        @Test
        @DisplayName("Cancelled bookings should not exclude slots")
        void cancelledBookingsShouldNotExcludeSlots() {
            // Saturday Jan 13, 2024 at 07:00 to ensure 2h min advance is met
            LocalDateTime now = LocalDateTime.of(2024, 1, 13, 7, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 13);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            SlotId previouslyCancelledSlotId = SlotId.generate();
            // 11h is 4h from 07:00, so it meets the 2h minimum advance
            Slot previouslyCancelledSlot = createSlotWithId(previouslyCancelledSlotId, DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5);

            List<Slot> candidates = List.of(previouslyCancelledSlot);

            // User has a cancelled booking for this slot
            List<Booking> userBookings = List.of(
                    createBooking(previouslyCancelledSlotId, userId, BookingStatus.CANCELLED)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, userBookings, now
            );

            // Cancelled booking should not prevent suggestion
            assertThat(suggestions).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Capacity Validation")
    class CapacityValidationTests {

        @Test
        @DisplayName("Full slots should be excluded from suggestions")
        void fullSlotsShouldBeExcluded() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            List<Slot> candidates = List.of(
                    // Full slot
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 10),
                    // Available slot
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(12, 0), 10, 5)
            );

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now
            );

            assertThat(suggestions).hasSize(1);
            assertThat(suggestions.get(0).slot().getTimeSlot().startTime()).isEqualTo(LocalTime.of(12, 0));
        }
    }

    @Nested
    @DisplayName("Max Suggestions Limit")
    class MaxSuggestionsTests {

        @Test
        @DisplayName("Should limit suggestions to max count")
        void shouldLimitSuggestionsToMaxCount() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 7, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);

            // Create 10 valid candidates
            List<Slot> candidates = List.of(
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(11, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(12, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(13, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(14, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(15, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(16, 0), 10, 5),
                    createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(17, 0), 10, 5)
            );

            // Request max 3 suggestions
            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, candidates, userId, Collections.emptyList(), now, 3
            );

            assertThat(suggestions).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Suggestion Type Classification")
    class SuggestionTypeTests {

        @Test
        @DisplayName("Same day later should be classified correctly")
        void sameDayLaterShouldBeClassifiedCorrectly() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 7, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);
            Slot laterSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(14, 0), 10, 5);

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, List.of(laterSlot), userId, Collections.emptyList(), now
            );

            assertThat(suggestions).hasSize(1);
            assertThat(suggestions.get(0).type()).isEqualTo(SuggestionType.SAME_DAY_LATER);
        }

        @Test
        @DisplayName("Next day same time should be classified correctly")
        void nextDaySameTimeShouldBeClassifiedCorrectly() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 7, 0);
            LocalDate saturday = LocalDate.of(2024, 1, 15);
            LocalDate monday = LocalDate.of(2024, 1, 17); // Skip Sunday for DRIVE

            Slot requestedSlot = createSlot(DeliveryMode.DRIVE, saturday, LocalTime.of(10, 0), 10, 10);
            Slot nextDaySlot = createSlot(DeliveryMode.DRIVE, monday, LocalTime.of(10, 0), 10, 5);

            List<SlotSuggestion> suggestions = suggestionService.findValidSuggestions(
                    requestedSlot, List.of(nextDaySlot), userId, Collections.emptyList(), now
            );

            assertThat(suggestions).hasSize(1);
            // Will be NEXT_AVAILABLE_SAME_TIME since Monday is not "next day" (Sunday skipped)
            assertThat(suggestions.get(0).type()).isEqualTo(SuggestionType.NEXT_AVAILABLE_SAME_TIME);
        }
    }

    @Nested
    @DisplayName("Explain No Suggestions")
    class ExplainNoSuggestionsTests {

        @Test
        @DisplayName("Should explain max bookings reached")
        void shouldExplainMaxBookingsReached() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

            List<String> reasons = suggestionService.explainNoSuggestions(
                    DeliveryMode.DRIVE, now, 3
            );

            assertThat(reasons).contains("Vous avez atteint le maximum de 3 réservations actives");
        }

        @Test
        @DisplayName("Should explain DELIVERY_TODAY cutoff")
        void shouldExplainDeliveryTodayCutoff() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 15, 20, 0); // After cutoff

            List<String> reasons = suggestionService.explainNoSuggestions(
                    DeliveryMode.DELIVERY_TODAY, now, 0
            );

            assertThat(reasons).anyMatch(r -> r.contains("heure limite"));
        }

        @Test
        @DisplayName("Should provide recommendation for different mode")
        void shouldProvideRecommendation() {
            String recommendation = suggestionService.getRecommendation(DeliveryMode.DELIVERY_TODAY);

            assertThat(recommendation).containsIgnoringCase("DELIVERY");
        }
    }

    // ========== HELPER METHODS ==========

    private Slot createSlot(DeliveryMode mode, LocalDate date, LocalTime startTime, int capacity, int bookedCount) {
        TimeSlot timeSlot = TimeSlot.of(startTime, mode.getSlotDuration());
        return Slot.reconstitute(SlotId.generate(), mode, date, timeSlot, capacity, bookedCount);
    }

    private Slot createSlotWithId(SlotId id, DeliveryMode mode, LocalDate date, LocalTime startTime, int capacity, int bookedCount) {
        TimeSlot timeSlot = TimeSlot.of(startTime, mode.getSlotDuration());
        return Slot.reconstitute(id, mode, date, timeSlot, capacity, bookedCount);
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
