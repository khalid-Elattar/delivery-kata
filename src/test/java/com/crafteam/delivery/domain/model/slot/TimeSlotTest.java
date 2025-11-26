package com.crafteam.delivery.domain.model.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TimeSlot Value Object")
class TimeSlotTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create TimeSlot with valid times")
        void shouldCreateTimeSlotWithValidTimes() {
            // Given
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(11, 0);

            // When
            TimeSlot timeSlot = new TimeSlot(start, end);

            // Then
            assertThat(timeSlot.startTime()).isEqualTo(start);
            assertThat(timeSlot.endTime()).isEqualTo(end);
        }

        @Test
        @DisplayName("should throw exception when end time is before start time")
        void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
            // Given
            LocalTime start = LocalTime.of(11, 0);
            LocalTime end = LocalTime.of(9, 0);

            // When/Then
            assertThatThrownBy(() -> new TimeSlot(start, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("End time")
                    .hasMessageContaining("must be after start time");
        }

        @Test
        @DisplayName("should throw exception when end time equals start time")
        void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
            // Given
            LocalTime time = LocalTime.of(9, 0);

            // When/Then
            assertThatThrownBy(() -> new TimeSlot(time, time))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception for null start time")
        void shouldThrowExceptionForNullStartTime() {
            // When/Then
            assertThatThrownBy(() -> new TimeSlot(null, LocalTime.of(10, 0)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Start time is required");
        }

        @Test
        @DisplayName("should throw exception for null end time")
        void shouldThrowExceptionForNullEndTime() {
            // When/Then
            assertThatThrownBy(() -> new TimeSlot(LocalTime.of(9, 0), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("End time is required");
        }
    }

    @Nested
    @DisplayName("Factory Method")
    class FactoryMethod {

        @Test
        @DisplayName("should create TimeSlot from start time and duration")
        void shouldCreateTimeSlotFromStartTimeAndDuration() {
            // Given
            LocalTime start = LocalTime.of(9, 0);
            Duration duration = Duration.ofHours(2);

            // When
            TimeSlot timeSlot = TimeSlot.of(start, duration);

            // Then
            assertThat(timeSlot.startTime()).isEqualTo(start);
            assertThat(timeSlot.endTime()).isEqualTo(LocalTime.of(11, 0));
        }
    }

    @Nested
    @DisplayName("Duration")
    class DurationTests {

        @Test
        @DisplayName("should calculate duration correctly")
        void shouldCalculateDurationCorrectly() {
            // Given
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 30));

            // When
            Duration duration = timeSlot.duration();

            // Then
            assertThat(duration).isEqualTo(Duration.ofMinutes(150));
        }
    }

    @Nested
    @DisplayName("Contains")
    class ContainsTests {

        @Test
        @DisplayName("should return true when time is within slot")
        void shouldReturnTrueWhenTimeIsWithinSlot() {
            // Given
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 0));

            // Then
            assertThat(timeSlot.contains(LocalTime.of(10, 0))).isTrue();
            assertThat(timeSlot.contains(LocalTime.of(9, 0))).isTrue(); // start inclusive
        }

        @Test
        @DisplayName("should return false when time is outside slot")
        void shouldReturnFalseWhenTimeIsOutsideSlot() {
            // Given
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 0));

            // Then
            assertThat(timeSlot.contains(LocalTime.of(8, 0))).isFalse();
            assertThat(timeSlot.contains(LocalTime.of(11, 0))).isFalse(); // end exclusive
            assertThat(timeSlot.contains(LocalTime.of(12, 0))).isFalse();
        }
    }

    @Nested
    @DisplayName("Overlaps")
    class OverlapsTests {

        @Test
        @DisplayName("should detect overlapping slots")
        void shouldDetectOverlappingSlots() {
            // Given
            TimeSlot slot1 = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 0));
            TimeSlot slot2 = new TimeSlot(LocalTime.of(10, 0), LocalTime.of(12, 0));

            // Then
            assertThat(slot1.overlaps(slot2)).isTrue();
            assertThat(slot2.overlaps(slot1)).isTrue();
        }

        @Test
        @DisplayName("should detect non-overlapping slots")
        void shouldDetectNonOverlappingSlots() {
            // Given
            TimeSlot slot1 = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
            TimeSlot slot2 = new TimeSlot(LocalTime.of(11, 0), LocalTime.of(12, 0));

            // Then
            assertThat(slot1.overlaps(slot2)).isFalse();
            assertThat(slot2.overlaps(slot1)).isFalse();
        }

        @Test
        @DisplayName("should detect adjacent slots as non-overlapping")
        void shouldDetectAdjacentSlotsAsNonOverlapping() {
            // Given
            TimeSlot slot1 = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
            TimeSlot slot2 = new TimeSlot(LocalTime.of(10, 0), LocalTime.of(11, 0));

            // Then
            assertThat(slot1.overlaps(slot2)).isFalse();
        }
    }

    @Nested
    @DisplayName("Formatted")
    class FormattedTests {

        @Test
        @DisplayName("should format TimeSlot correctly")
        void shouldFormatTimeSlotCorrectly() {
            // Given
            TimeSlot timeSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(11, 30));

            // When
            String formatted = timeSlot.formatted();

            // Then
            assertThat(formatted).isEqualTo("09:00 - 11:30");
        }
    }
}
