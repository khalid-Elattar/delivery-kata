package com.crafteam.delivery.domain.model.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliveryMode Enum")
class DeliveryModeTest {

    @Nested
    @DisplayName("Slot Duration")
    class SlotDurationTests {

        @Test
        @DisplayName("DRIVE should have 60 minute slots")
        void driveShouldHave60MinuteSlots() {
            assertThat(DeliveryMode.DRIVE.getSlotDuration())
                    .isEqualTo(Duration.ofMinutes(60));
        }

        @Test
        @DisplayName("DELIVERY should have 120 minute slots")
        void deliveryShouldHave120MinuteSlots() {
            assertThat(DeliveryMode.DELIVERY.getSlotDuration())
                    .isEqualTo(Duration.ofMinutes(120));
        }

        @Test
        @DisplayName("DELIVERY_TODAY should have 60 minute slots")
        void deliveryTodayShouldHave60MinuteSlots() {
            assertThat(DeliveryMode.DELIVERY_TODAY.getSlotDuration())
                    .isEqualTo(Duration.ofMinutes(60));
        }

        @Test
        @DisplayName("DELIVERY_ASAP should have 30 minute slots")
        void deliveryAsapShouldHave30MinuteSlots() {
            assertThat(DeliveryMode.DELIVERY_ASAP.getSlotDuration())
                    .isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("Available Days")
    class AvailableDaysTests {

        @Test
        @DisplayName("DRIVE should be available Monday to Saturday")
        void driveShouldBeAvailableMondayToSaturday() {
            assertThat(DeliveryMode.DRIVE.getAvailableDays())
                    .containsExactlyInAnyOrder(
                            DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
                    );
        }

        @Test
        @DisplayName("DELIVERY should be available Monday to Friday")
        void deliveryShouldBeAvailableMondayToFriday() {
            assertThat(DeliveryMode.DELIVERY.getAvailableDays())
                    .containsExactlyInAnyOrder(
                            DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY
                    );
        }

        @Test
        @DisplayName("DELIVERY_TODAY should be available every day")
        void deliveryTodayShouldBeAvailableEveryDay() {
            assertThat(DeliveryMode.DELIVERY_TODAY.getAvailableDays()).hasSize(7);
        }

        @Test
        @DisplayName("DELIVERY_ASAP should be available every day")
        void deliveryAsapShouldBeAvailableEveryDay() {
            assertThat(DeliveryMode.DELIVERY_ASAP.getAvailableDays()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("isAvailableFor")
    class IsAvailableForTests {

        @Test
        @DisplayName("DRIVE should not be available on Sunday")
        void driveShouldNotBeAvailableOnSunday() {
            // Given
            LocalDate sunday = getNextDayOfWeek(DayOfWeek.SUNDAY);

            // Then
            assertThat(DeliveryMode.DRIVE.isAvailableFor(sunday)).isFalse();
        }

        @Test
        @DisplayName("DELIVERY should not be available on weekend")
        void deliveryShouldNotBeAvailableOnWeekend() {
            // Given
            LocalDate saturday = getNextDayOfWeek(DayOfWeek.SATURDAY);
            LocalDate sunday = getNextDayOfWeek(DayOfWeek.SUNDAY);

            // Then
            assertThat(DeliveryMode.DELIVERY.isAvailableFor(saturday)).isFalse();
            assertThat(DeliveryMode.DELIVERY.isAvailableFor(sunday)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(DeliveryMode.class)
        @DisplayName("DELIVERY_TODAY and DELIVERY_ASAP should be available on any day")
        void todayAndAsapShouldBeAvailableAnyDay(DeliveryMode mode) {
            // Given
            LocalDate monday = getNextDayOfWeek(DayOfWeek.MONDAY);

            // Then
            if (mode == DeliveryMode.DELIVERY_TODAY || mode == DeliveryMode.DELIVERY_ASAP) {
                assertThat(mode.isAvailableFor(monday)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("isValidDate")
    class IsValidDateTests {

        @Test
        @DisplayName("DELIVERY_TODAY should only be valid for today")
        void deliveryTodayShouldOnlyBeValidForToday() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            // Then
            assertThat(DeliveryMode.DELIVERY_TODAY.isValidDate(today, today)).isTrue();
            assertThat(DeliveryMode.DELIVERY_TODAY.isValidDate(tomorrow, today)).isFalse();
        }

        @Test
        @DisplayName("DRIVE should be valid for today and future dates")
        void driveShouldBeValidForTodayAndFuture() {
            // Given
            LocalDate today = getNextDayOfWeek(DayOfWeek.MONDAY);
            LocalDate tomorrow = today.plusDays(1);

            // Then
            assertThat(DeliveryMode.DRIVE.isValidDate(today, today)).isTrue();
            assertThat(DeliveryMode.DRIVE.isValidDate(tomorrow, today)).isTrue();
        }

        @Test
        @DisplayName("DRIVE should not be valid for past dates")
        void driveShouldNotBeValidForPastDates() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // When finding a valid past weekday
            while (!DeliveryMode.DRIVE.isAvailableFor(yesterday)) {
                yesterday = yesterday.minusDays(1);
            }

            // Then
            assertThat(DeliveryMode.DRIVE.isValidDate(yesterday, today)).isFalse();
        }
    }

    private LocalDate getNextDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}
