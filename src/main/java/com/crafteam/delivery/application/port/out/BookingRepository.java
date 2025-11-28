package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Output port for booking persistence operations.
 */
public interface BookingRepository {

    Mono<Booking> findById(BookingId id);

    Flux<Booking> findByUserId(UserId userId);

    Flux<Booking> findBySlotId(SlotId slotId);

    /**
     * Count active bookings for a specific slot, date, and time.
     * Used to check if capacity is available.
     *
     * @param slotId The slot template ID
     * @param date The booking date
     * @param time The booking time
     * @return Count of active (non-cancelled) bookings
     */
    Mono<Long> countBySlotIdAndDateAndTime(SlotId slotId, LocalDate date, LocalTime time);

    /**
     * Check if a user already has an active booking for the same slot/date/time.
     *
     * @param slotId The slot template ID
     * @param userId The user ID
     * @param date The booking date
     * @param time The booking time
     * @return true if the user already has a booking, false otherwise
     */
    Mono<Boolean> existsBySlotIdAndUserIdAndDateAndTime(
            SlotId slotId,
            UserId userId,
            LocalDate date,
            LocalTime time
    );

    /**
     * Count active bookings for a user.
     * Used to enforce the maximum active bookings limit.
     *
     * @param userId The user ID
     * @return Count of active (non-cancelled) bookings for the user
     */
    Mono<Long> countActiveByUserId(UserId userId);

    /**
     * Find all booked times for a specific slot and date.
     * Used for generating alternative slot suggestions.
     *
     * @param slotId The slot template ID
     * @param date The booking date
     * @return Flux of booking times
     */
    Flux<LocalTime> findBookedTimesForDate(SlotId slotId, LocalDate date);

    Mono<Booking> save(Booking booking);

    Mono<Void> delete(BookingId id);
}
