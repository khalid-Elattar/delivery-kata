package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.domain.model.booking.Booking;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Input port for retrieving bookings.
 */
public interface GetBookingUseCase {
    Mono<Booking> findById(String bookingId);

    Flux<Booking> findByCustomerId(String customerId);
}
