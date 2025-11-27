package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalTime;
import java.util.Map;

/**
 * Exception thrown when the slot time is not within the allowed range for the delivery mode.
 */
public class InvalidTimeForModeException extends BookingValidationException {

    public InvalidTimeForModeException(DeliveryMode mode, LocalTime requestedTime) {
        super(
                "INVALID_TIME_FOR_MODE",
                "L'heure %s n'est pas dans la plage horaire du mode %s (%s-%s)".formatted(
                        requestedTime, mode.name(), mode.getStartTime(), mode.getEndTime()),
                Map.of(
                        "mode", mode.name(),
                        "requestedTime", requestedTime.toString(),
                        "allowedStartTime", mode.getStartTime().toString(),
                        "allowedEndTime", mode.getEndTime().toString()
                )
        );
    }
}
