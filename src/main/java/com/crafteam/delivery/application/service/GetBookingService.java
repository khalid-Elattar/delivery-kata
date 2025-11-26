package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.in.GetBookingUseCase;
import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.domain.exception.BookingNotFoundException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Application service for retrieving bookings.
 */
@Service
public class GetBookingService implements GetBookingUseCase {

    private final BookingRepository bookingRepository;

    public GetBookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Mono<Booking> findById(String bookingId) {
        return bookingRepository.findById(BookingId.from(bookingId))
                .switchIfEmpty(Mono.error(new BookingNotFoundException(bookingId)));
    }

    @Override
    public Flux<Booking> findByCustomerId(String customerId) {
        return bookingRepository.findByCustomerId(CustomerId.from(customerId));
    }
}
