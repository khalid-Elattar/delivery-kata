package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Exception thrown when booking does not meet minimum advance time requirement.
 */
public class MinAdvanceTimeException extends BookingValidationException {

    public MinAdvanceTimeException(DeliveryMode mode, LocalDateTime slotDateTime, LocalDateTime currentTime) {
        super(
                "MIN_ADVANCE_TIME_NOT_MET",
                buildMessage(mode),
                Map.of(
                        "mode", mode.name(),
                        "requiredAdvance", getAdvanceDescription(mode),
                        "requestedSlotTime", slotDateTime.toString(),
                        "currentTime", currentTime.toString()
                )
        );
    }

    private static String buildMessage(DeliveryMode mode) {
        String advanceDescription = getAdvanceDescription(mode);
        return "Le mode %s nécessite une réservation au moins %s à l'avance".formatted(
                mode.name(), advanceDescription);
    }

    private static String getAdvanceDescription(DeliveryMode mode) {
        if (mode == DeliveryMode.DELIVERY_ASAP) {
            return "30 minutes";
        }
        int hours = mode.getMinAdvanceHours();
        if (hours >= 24) {
            int days = hours / 24;
            return days + (days > 1 ? " jours" : " jour");
        }
        return hours + (hours > 1 ? " heures" : " heure");
    }
}
