package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.AcceptSuggestionCommand;
import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.dto.response.BookingResult;
import com.crafteam.delivery.application.port.in.AcceptSuggestionUseCase;
import com.crafteam.delivery.application.port.in.BookSlotWithSuggestionsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for accepting a suggested slot.
 *
 * <p>Delegates to {@link BookSlotWithSuggestionsUseCase} to handle all validation
 * and potential race conditions (slot may have become unavailable since suggestion).</p>
 */
@Service
@Transactional
public class AcceptSuggestionService implements AcceptSuggestionUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcceptSuggestionService.class);

    private final BookSlotWithSuggestionsUseCase bookSlotWithSuggestionsUseCase;

    public AcceptSuggestionService(BookSlotWithSuggestionsUseCase bookSlotWithSuggestionsUseCase) {
        this.bookSlotWithSuggestionsUseCase = bookSlotWithSuggestionsUseCase;
    }

    @Override
    public Mono<BookingResult> execute(AcceptSuggestionCommand command) {
        log.info("Accepting suggestion: slotId={}, userId={}, originalSlotId={}",
                command.slotId(), command.userId(), command.originalSlotId());

        // Re-use the booking with suggestions logic to handle concurrency
        // If the suggested slot is now unavailable, new suggestions will be returned
        BookSlotCommand bookCommand = new BookSlotCommand(command.slotId(), command.userId());

        return bookSlotWithSuggestionsUseCase.execute(bookCommand)
                .doOnSuccess(result -> {
                    if (result.isSuccessful()) {
                        log.info("Suggestion accepted successfully: bookingId={}",
                                result.booking().getId());
                    } else {
                        log.info("Suggestion no longer available, new suggestions provided: status={}",
                                result.status());
                    }
                });
    }
}
