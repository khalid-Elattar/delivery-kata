package com.crafteam.delivery.infrastructure.exception;

import com.crafteam.delivery.domain.exception.BookingNotFoundException;
import com.crafteam.delivery.domain.exception.SlotNotAvailableException;
import com.crafteam.delivery.domain.exception.SlotNotFoundException;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SlotNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSlotNotFound(SlotNotFoundException ex) {
        log.warn("Slot not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "SLOT_NOT_FOUND",
                        ex.getMessage(),
                        Instant.now()
                )));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBookingNotFound(BookingNotFoundException ex) {
        log.warn("Booking not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "BOOKING_NOT_FOUND",
                        ex.getMessage(),
                        Instant.now()
                )));
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSlotNotAvailable(SlotNotAvailableException ex) {
        log.warn("Slot not available: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "SLOT_NOT_AVAILABLE",
                        ex.getMessage(),
                        Instant.now()
                )));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(WebExchangeBindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        message,
                        Instant.now()
                )));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_ARGUMENT",
                        ex.getMessage(),
                        Instant.now()
                )));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR",
                        "An unexpected error occurred",
                        Instant.now()
                )));
    }
}
