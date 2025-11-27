package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.util.Map;

/**
 * Exception thrown when the selected date is not valid for the delivery mode.
 */
public class InvalidDateForModeException extends BookingValidationException {

    public InvalidDateForModeException(DeliveryMode mode, LocalDate date) {
        super(
                "INVALID_DATE_FOR_MODE",
                "La date %s n'est pas valide pour le mode %s".formatted(date, mode.name()),
                Map.of(
                        "mode", mode.name(),
                        "requestedDate", date.toString(),
                        "availableDays", mode.getAvailableDays().toString()
                )
        );
    }

    public InvalidDateForModeException(DeliveryMode mode, LocalDate date, String reason) {
        super(
                "INVALID_DATE_FOR_MODE",
                reason,
                Map.of(
                        "mode", mode.name(),
                        "requestedDate", date.toString()
                )
        );
    }
}
