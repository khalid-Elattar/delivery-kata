package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.CancelBookingCommand;
import reactor.core.publisher.Mono;

/**
 * Input port for cancelling a booking.
 */
public interface CancelBookingUseCase {
    Mono<Void> execute(CancelBookingCommand command);
}
