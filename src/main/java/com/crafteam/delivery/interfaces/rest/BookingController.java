package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.CancelBookingCommand;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.in.CancelBookingUseCase;
import com.crafteam.delivery.application.port.in.GetBookingUseCase;
import com.crafteam.delivery.interfaces.rest.dto.request.BookSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.BookingResponse;
import com.crafteam.delivery.interfaces.rest.hateoas.BookingModelAssembler;
import com.crafteam.delivery.interfaces.rest.mapper.BookingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for booking management.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Booking management API")
public class BookingController {

    private final BookSlotUseCase bookSlotUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;
    private final BookingModelAssembler bookingModelAssembler;
    private final BookingWebMapper bookingWebMapper;

    public BookingController(BookSlotUseCase bookSlotUseCase,
                             CancelBookingUseCase cancelBookingUseCase,
                             GetBookingUseCase getBookingUseCase,
                             BookingModelAssembler bookingModelAssembler,
                             BookingWebMapper bookingWebMapper) {
        this.bookSlotUseCase = bookSlotUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
        this.getBookingUseCase = getBookingUseCase;
        this.bookingModelAssembler = bookingModelAssembler;
        this.bookingWebMapper = bookingWebMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
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
        return bookSlotUseCase.execute(bookingWebMapper.toCommand(request))
                .map(bookingWebMapper::toResponse)
                .map(bookingModelAssembler::toModel);
    }

    @GetMapping(value = "/{bookingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get booking by ID", description = "Retrieves a specific booking")
    @ApiResponse(responseCode = "200", description = "Booking found")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public Mono<EntityModel<BookingResponse>> getBooking(@PathVariable String bookingId) {
        return getBookingUseCase.findById(bookingId)
                .map(bookingWebMapper::toResponse)
                .map(bookingModelAssembler::toModel);
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get bookings by user", description = "Retrieves all bookings for a user")
    public Mono<CollectionModel<EntityModel<BookingResponse>>> getUserBookings(
            @PathVariable String userId) {
        return getBookingUseCase.findByUserId(userId)
                .map(bookingWebMapper::toResponse)
                .map(bookingModelAssembler::toModel)
                .collectList()
                .map(CollectionModel::of);
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
