package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.AcceptSuggestionCommand;
import com.crafteam.delivery.application.dto.response.BookingResult;
import reactor.core.publisher.Mono;

/**
 * Input port for accepting a suggested slot.
 *
 * <p>When a booking attempt fails and suggestions are returned,
 * this use case allows the user to book one of the suggested slots.</p>
 */
public interface AcceptSuggestionUseCase {

    /**
     * Accepts a suggested slot and creates a booking.
     *
     * <p>This performs all validations again to handle concurrency
     * (the suggested slot may have become unavailable since the suggestion was made).</p>
     *
     * @param command The accept suggestion command
     * @return A BookingResult with either success or new suggestions if the slot became unavailable
     */
    Mono<BookingResult> execute(AcceptSuggestionCommand command);
}
