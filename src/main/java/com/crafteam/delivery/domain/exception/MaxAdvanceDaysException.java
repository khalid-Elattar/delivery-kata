package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.util.Map;

/**
 * Exception thrown when booking exceeds maximum advance days limit.
 */
public class MaxAdvanceDaysException extends BookingValidationException {

    public MaxAdvanceDaysException(DeliveryMode mode, LocalDate slotDate, LocalDate today) {
        super(
                "MAX_ADVANCE_DAYS_EXCEEDED",
                "Le mode %s ne permet pas de réserver plus de %d jours à l'avance".formatted(
                        mode.name(), mode.getMaxAdvanceDays()),
                Map.of(
                        "mode", mode.name(),
                        "maxAdvanceDays", mode.getMaxAdvanceDays(),
                        "requestedDate", slotDate.toString(),
                        "currentDate", today.toString()
                )
        );
    }
}
