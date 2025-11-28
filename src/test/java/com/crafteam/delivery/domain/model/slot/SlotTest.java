package com.crafteam.delivery.domain.model.slot;

import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Slot Aggregate")
class SlotTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create slot template with valid parameters")
        void shouldCreateSlotWithValidParameters() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            LocalTime startTime = LocalTime.of(8, 0);
            LocalTime endTime = LocalTime.of(20, 0);
            Duration slotDuration = Duration.ofMinutes(60);
            int capacity = 10;

            // When
            Slot slot = Slot.create(mode, availableDays, startTime, endTime, slotDuration, capacity);

            // Then
            assertThat(slot.getId()).isNotNull();
            assertThat(slot.getDeliveryMode()).isEqualTo(mode);
            assertThat(slot.getAvailableDays()).isEqualTo(availableDays);
            assertThat(slot.getStartTime()).isEqualTo(startTime);
            assertThat(slot.getEndTime()).isEqualTo(endTime);
            assertThat(slot.getSlotDuration()).isEqualTo(slotDuration);
            assertThat(slot.getCapacity()).isEqualTo(capacity);
        }

        @Test
        @DisplayName("should emit SlotCreatedEvent on creation")
        void shouldEmitSlotCreatedEventOnCreation() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
            Duration slotDuration = Duration.ofMinutes(60);

            // When
            Slot slot = Slot.create(mode, availableDays, startTime, endTime, slotDuration, 5);

            // Then
            assertThat(slot.getDomainEvents()).hasSize(1);
            assertThat(slot.getDomainEvents().get(0)).isInstanceOf(SlotCreatedEvent.class);
            SlotCreatedEvent event = (SlotCreatedEvent) slot.getDomainEvents().get(0);
            assertThat(event.slotId()).isEqualTo(slot.getId());
            assertThat(event.deliveryMode()).isEqualTo(mode);
            assertThat(event.availableDays()).isEqualTo(availableDays);
        }

        @Test
        @DisplayName("should throw exception for invalid capacity")
        void shouldThrowExceptionForInvalidCapacity() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY);
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
            Duration slotDuration = Duration.ofMinutes(60);

            // When/Then
            assertThatThrownBy(() -> Slot.create(mode, availableDays, startTime, endTime, slotDuration, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Capacity must be positive");
        }

        @Test
        @DisplayName("should throw exception for empty available days")
        void shouldThrowExceptionForEmptyAvailableDays() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> emptyDays = Set.of();
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
            Duration slotDuration = Duration.ofMinutes(60);

            // When/Then
            assertThatThrownBy(() -> Slot.create(mode, emptyDays, startTime, endTime, slotDuration, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one available day is required");
        }

        @Test
        @DisplayName("should throw exception for invalid time range")
        void shouldThrowExceptionForInvalidTimeRange() {
            // Given
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY);
            LocalTime startTime = LocalTime.of(17, 0);
            LocalTime endTime = LocalTime.of(9, 0); // End before start
            Duration slotDuration = Duration.ofMinutes(60);

            // When/Then
            assertThatThrownBy(() -> Slot.create(mode, availableDays, startTime, endTime, slotDuration, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start time must be before end time");
        }
    }

    @Nested
    @DisplayName("Day Availability")
    class DayAvailabilityTests {

        @Test
        @DisplayName("should return true for available day")
        void shouldReturnTrueForAvailableDay() {
            // Given
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
            Slot slot = Slot.create(DeliveryMode.DRIVE, availableDays,
                    LocalTime.of(8, 0), LocalTime.of(20, 0), Duration.ofMinutes(60), 10);

            // When/Then
            assertThat(slot.isAvailableOn(DayOfWeek.MONDAY)).isTrue();
            assertThat(slot.isAvailableOn(DayOfWeek.WEDNESDAY)).isTrue();
        }

        @Test
        @DisplayName("should return false for unavailable day")
        void shouldReturnFalseForUnavailableDay() {
            // Given
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
            Slot slot = Slot.create(DeliveryMode.DRIVE, availableDays,
                    LocalTime.of(8, 0), LocalTime.of(20, 0), Duration.ofMinutes(60), 10);

            // When/Then
            assertThat(slot.isAvailableOn(DayOfWeek.SUNDAY)).isFalse();
            assertThat(slot.isAvailableOn(DayOfWeek.TUESDAY)).isFalse();
        }
    }

    @Nested
    @DisplayName("Time Validation")
    class TimeValidationTests {

        @Test
        @DisplayName("should validate aligned booking times")
        void shouldValidateAlignedBookingTimes() {
            // Given - Slot from 08:00 to 20:00 with 60min duration
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10);

            // When/Then - Valid times (aligned with grid)
            assertThat(slot.isValidBookingTime(LocalTime.of(8, 0))).isTrue();
            assertThat(slot.isValidBookingTime(LocalTime.of(9, 0))).isTrue();
            assertThat(slot.isValidBookingTime(LocalTime.of(19, 0))).isTrue(); // Last valid slot
        }

        @Test
        @DisplayName("should reject unaligned booking times")
        void shouldRejectUnalignedBookingTimes() {
            // Given - Slot from 08:00 to 20:00 with 60min duration
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10);

            // When/Then - Invalid times (not aligned with grid)
            assertThat(slot.isValidBookingTime(LocalTime.of(8, 30))).isFalse();
            assertThat(slot.isValidBookingTime(LocalTime.of(9, 15))).isFalse();
        }

        @Test
        @DisplayName("should reject times outside operating hours")
        void shouldRejectTimesOutsideOperatingHours() {
            // Given - Slot from 08:00 to 20:00 with 60min duration
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10);

            // When/Then
            assertThat(slot.isValidBookingTime(LocalTime.of(7, 0))).isFalse(); // Before start
            assertThat(slot.isValidBookingTime(LocalTime.of(20, 0))).isFalse(); // No room for full duration
        }

        @Test
        @DisplayName("should get all valid booking times")
        void shouldGetAllValidBookingTimes() {
            // Given - Slot from 08:00 to 12:00 with 60min duration
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(12, 0),
                    Duration.ofMinutes(60),
                    10);

            // When
            var validTimes = slot.getValidBookingTimes();

            // Then - Should return 08:00, 09:00, 10:00, 11:00 (4 slots)
            assertThat(validTimes).containsExactly(
                    LocalTime.of(8, 0),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0)
            );
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("should create slot from delivery mode defaults")
        void shouldCreateSlotFromDeliveryModeDefaults() {
            // When
            Slot slot = Slot.createFromMode(DeliveryMode.DRIVE);

            // Then
            assertThat(slot.getDeliveryMode()).isEqualTo(DeliveryMode.DRIVE);
            assertThat(slot.getAvailableDays()).isEqualTo(DeliveryMode.DRIVE.getAvailableDays());
            assertThat(slot.getStartTime()).isEqualTo(DeliveryMode.DRIVE.getStartTime());
            assertThat(slot.getEndTime()).isEqualTo(DeliveryMode.DRIVE.getEndTime());
            assertThat(slot.getSlotDuration()).isEqualTo(DeliveryMode.DRIVE.getSlotDuration());
            assertThat(slot.getCapacity()).isEqualTo(DeliveryMode.DRIVE.getDefaultCapacity());
        }

        @Test
        @DisplayName("should reconstitute slot from persistence")
        void shouldReconstituteSlotFromPersistence() {
            // Given
            SlotId id = SlotId.generate();
            DeliveryMode mode = DeliveryMode.DRIVE;
            Set<DayOfWeek> availableDays = Set.of(DayOfWeek.MONDAY);
            LocalTime startTime = LocalTime.of(8, 0);
            LocalTime endTime = LocalTime.of(20, 0);
            Duration slotDuration = Duration.ofMinutes(60);
            int capacity = 10;

            // When
            Slot slot = Slot.reconstitute(id, mode, availableDays, startTime, endTime, slotDuration, capacity);

            // Then
            assertThat(slot.getId()).isEqualTo(id);
            assertThat(slot.getDeliveryMode()).isEqualTo(mode);
            assertThat(slot.getDomainEvents()).isEmpty(); // Reconstituted slots have no events
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethodsTests {

        @Test
        @DisplayName("should calculate end time correctly")
        void shouldCalculateEndTimeCorrectly() {
            // Given
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10);

            // When
            LocalTime endTime = slot.calculateEndTime(LocalTime.of(10, 0));

            // Then
            assertThat(endTime).isEqualTo(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("should return slot duration in minutes")
        void shouldReturnSlotDurationInMinutes() {
            // Given
            Slot slot = Slot.create(DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(90),
                    10);

            // When
            long durationMinutes = slot.getSlotDurationMinutes();

            // Then
            assertThat(durationMinutes).isEqualTo(90);
        }
    }
}
