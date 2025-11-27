package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalTime;
import java.util.Map;

/**
 * Exception thrown when the cutoff time for DELIVERY_TODAY has passed.
 */
public class CutoffTimePassedException extends BookingValidationException {

    public CutoffTimePassedException(LocalTime cutoffTime, LocalTime currentTime) {
        super(
                "CUTOFF_TIME_PASSED",
                "L'heure limite de commande (%s) est dépassée pour DELIVERY_TODAY".formatted(cutoffTime),
                Map.of(
                        "mode", DeliveryMode.DELIVERY_TODAY.name(),
                        "cutoffTime", cutoffTime.toString(),
                        "currentTime", currentTime.toString()
                )
        );
    }
}
