package com.crafteam.delivery.infrastructure.adapter.in.web;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.request.BookSlotRequest;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.BookingResponse;
import com.crafteam.delivery.infrastructure.adapter.in.web.mapper.BookingWebMapper;
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
                        booking.getCustomerId().toString(),
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
                        request.customerId()
                );
            }
        };
    }
}
