package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.interfaces.rest.dto.request.BookSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.BookingResponse;
import com.crafteam.delivery.interfaces.rest.mapper.BookingWebMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration to provide BookingWebMapper for WebFluxTest.
 */
@TestConfiguration
public class BookingWebMapperTestConfig {

    @Bean
    public BookingWebMapper bookingWebMapper() {
        return new BookingWebMapper() {
            @Override
            public BookingResponse toResponse(Booking booking) {
                if (booking == null) {
                    return null;
                }
                return new BookingResponse(
                        booking.getId().toString(),
                        booking.getSlotId().toString(),
                        booking.getUserId().toString(),
                        booking.getStatus(),
                        booking.getCreatedAt(),
                        booking.getConfirmedAt(),
                        booking.getCancelledAt()
                );
            }

            @Override
            public BookSlotCommand toCommand(BookSlotRequest request) {
                if (request == null) {
                    return null;
                }
                return new BookSlotCommand(
                        request.slotId(),
                        request.userId()
                );
            }
        };
    }
}
