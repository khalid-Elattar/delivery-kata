package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.dto.response.BookingResult;
import reactor.core.publisher.Mono;

/**
 * Input port for booking a delivery slot with suggestion support.
 *
 * <p>When the requested slot is unavailable, this use case returns
 * alternative slot suggestions that respect all business rules.</p>
 */
public interface BookSlotWithSuggestionsUseCase {

    /**
     * Attempts to book a slot. If unavailable, returns suggestions.
     *
     * @param command The booking command
     * @return A BookingResult containing either the booking or suggestions
     */
    Mono<BookingResult> execute(BookSlotCommand command);
}
