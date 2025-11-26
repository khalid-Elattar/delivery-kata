package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.domain.model.booking.Booking;
import reactor.core.publisher.Mono;

/**
 * Input port for booking a delivery slot.
 */
public interface BookSlotUseCase {
    Mono<Booking> execute(BookSlotCommand command);
}
