package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.booking.BookingId;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Exception thrown when a booking cannot be cancelled (less than 1 hour before slot).
 */
public class CancellationNotAllowedException extends BookingValidationException {

    private static final int CANCELLATION_DEADLINE_HOURS = 1;

    public CancellationNotAllowedException(BookingId bookingId, LocalDateTime slotStartTime, LocalDateTime currentTime) {
        super(
                "CANCELLATION_NOT_ALLOWED",
                "Annulation impossible moins d'%d heure avant le début du créneau".formatted(CANCELLATION_DEADLINE_HOURS),
                Map.of(
                        "bookingId", bookingId.value().toString(),
                        "slotStartTime", slotStartTime.toString(),
                        "currentTime", currentTime.toString(),
                        "cancellationDeadlineHours", CANCELLATION_DEADLINE_HOURS
                )
        );
    }
}
