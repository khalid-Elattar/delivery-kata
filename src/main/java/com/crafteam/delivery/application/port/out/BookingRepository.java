package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port for booking persistence operations.
 */
public interface BookingRepository {

    Mono<Booking> findById(BookingId id);

    Flux<Booking> findByUserId(UserId userId);

    Flux<Booking> findBySlotId(SlotId slotId);

    Mono<Booking> save(Booking booking);

    Mono<Void> delete(BookingId id);
}
