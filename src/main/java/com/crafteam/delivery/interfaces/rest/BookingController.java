package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.AcceptSuggestionCommand;
import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.dto.command.CancelBookingCommand;
import com.crafteam.delivery.application.dto.response.BookingResult;
import com.crafteam.delivery.application.port.in.AcceptSuggestionUseCase;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.in.BookSlotWithSuggestionsUseCase;
import com.crafteam.delivery.application.port.in.CancelBookingUseCase;
import com.crafteam.delivery.application.port.in.GetBookingUseCase;
import com.crafteam.delivery.infrastructure.security.CustomUserDetails;
import com.crafteam.delivery.interfaces.rest.dto.request.AcceptSuggestionRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.BookSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.BookingResponse;
import com.crafteam.delivery.interfaces.rest.dto.response.BookingResultResponse;
import com.crafteam.delivery.interfaces.rest.hateoas.BookingModelAssembler;
import com.crafteam.delivery.interfaces.rest.mapper.BookingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for booking management.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Booking management API")
public class BookingController {

    private final BookSlotUseCase bookSlotUseCase;
    private final BookSlotWithSuggestionsUseCase bookSlotWithSuggestionsUseCase;
    private final AcceptSuggestionUseCase acceptSuggestionUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;
    private final BookingWebMapper bookingWebMapper;
    private final BookingModelAssembler bookingModelAssembler;

    public BookingController(BookSlotUseCase bookSlotUseCase,
                             BookSlotWithSuggestionsUseCase bookSlotWithSuggestionsUseCase,
                             AcceptSuggestionUseCase acceptSuggestionUseCase,
                             CancelBookingUseCase cancelBookingUseCase,
                             GetBookingUseCase getBookingUseCase,
                             BookingWebMapper bookingWebMapper,
                             BookingModelAssembler bookingModelAssembler) {
        this.bookSlotUseCase = bookSlotUseCase;
        this.bookSlotWithSuggestionsUseCase = bookSlotWithSuggestionsUseCase;
        this.acceptSuggestionUseCase = acceptSuggestionUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
        this.getBookingUseCase = getBookingUseCase;
        this.bookingWebMapper = bookingWebMapper;
        this.bookingModelAssembler = bookingModelAssembler;
    }

    @PostMapping(produces = "application/hal+json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Book a delivery slot",
            description = "Creates a new booking for a specific slot and customer"
    )
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Slot not found")
    @ApiResponse(responseCode = "409", description = "Slot not available")
    public Mono<EntityModel<BookingResponse>> bookSlot(@Valid @RequestBody BookSlotRequest request) {
        // Extract userId from reactive security context
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    CustomUserDetails userDetails = (CustomUserDetails) securityContext.getAuthentication().getPrincipal();
                    return userDetails.getUserId();
                })
                .flatMap(userId -> {
                    BookSlotRequest requestWithUserId = new BookSlotRequest(
                            request.slotId(),
                            userId,
                            request.deliveryMode(),
                            request.date(),
                            request.time()
                    );
                    return bookSlotUseCase.execute(bookingWebMapper.toCommand(requestWithUserId))
                            .map(bookingWebMapper::toResponse)
                            .map(bookingModelAssembler::toModel);
                });
    }

    @PostMapping(value = "/with-suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Book a delivery slot with suggestions",
            description = "Attempts to book a slot. If unavailable, returns alternative slot suggestions that respect all business rules."
    )
    @ApiResponse(responseCode = "200", description = "Booking result (success or suggestions)")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Slot not found")
    public Mono<ResponseEntity<BookingResultResponse>> bookSlotWithSuggestions(
            @Valid @RequestBody BookSlotRequest request) {
        BookSlotCommand command = new BookSlotCommand(request.slotId(), request.userId());

        return bookSlotWithSuggestionsUseCase.execute(command)
                .map(result -> {
                    BookingResultResponse response = BookingResultResponse.from(result);
                    HttpStatus status = determineHttpStatus(result);
                    return ResponseEntity.status(status).body(response);
                });
    }

    @PostMapping(value = "/accept-suggestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Accept a suggested slot",
            description = "Books one of the previously suggested alternative slots. All validations are re-checked for concurrency safety."
    )
    @ApiResponse(responseCode = "200", description = "Booking result (success or new suggestions if slot became unavailable)")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Slot not found")
    public Mono<ResponseEntity<BookingResultResponse>> acceptSuggestion(
            @Valid @RequestBody AcceptSuggestionRequest request) {
        AcceptSuggestionCommand command = new AcceptSuggestionCommand(
                request.slotId(),
                request.userId(),
                request.originalSlotId()
        );

        return acceptSuggestionUseCase.execute(command)
                .map(result -> {
                    BookingResultResponse response = BookingResultResponse.from(result);
                    HttpStatus status = determineHttpStatus(result);
                    return ResponseEntity.status(status).body(response);
                });
    }

    private HttpStatus determineHttpStatus(BookingResult result) {
        return switch (result.status()) {
            case CONFIRMED -> HttpStatus.CREATED;
            case SLOT_UNAVAILABLE -> HttpStatus.OK;
            case NO_ALTERNATIVES -> HttpStatus.OK;
            case MAX_BOOKINGS_REACHED -> HttpStatus.CONFLICT;
        };
    }

    @GetMapping(value = "/{bookingId}", produces = "application/hal+json")
    @Operation(summary = "Get booking by ID", description = "Retrieves a specific booking with HATEOAS links")
    @ApiResponse(responseCode = "200", description = "Booking found")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public Mono<EntityModel<BookingResponse>> getBooking(@PathVariable String bookingId) {
        return getBookingUseCase.findById(bookingId)
                .map(bookingWebMapper::toResponse)
                .map(bookingModelAssembler::toModel);
    }

    @GetMapping(value = "/user/{userId}", produces = "application/hal+json")
    @Operation(summary = "Get bookings by user", description = "Retrieves all bookings for a user with HATEOAS links")
    public Flux<EntityModel<BookingResponse>> getUserBookings(@PathVariable String userId) {
        return getBookingUseCase.findByUserId(userId)
                .map(bookingWebMapper::toResponse)
                .map(bookingModelAssembler::toModel);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a booking", description = "Cancels an existing booking")
    @ApiResponse(responseCode = "204", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public Mono<Void> cancelBooking(@PathVariable String bookingId) {
        return cancelBookingUseCase.execute(new CancelBookingCommand(bookingId));
    }
}
