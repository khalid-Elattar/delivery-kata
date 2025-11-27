package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Exception thrown when booking exceeds the ASAP time window (4 hours max).
 */
public class AsapWindowExceededException extends BookingValidationException {

    public AsapWindowExceededException(LocalDateTime slotDateTime, LocalDateTime currentTime, int maxHours) {
        super(
                "ASAP_WINDOW_EXCEEDED",
                "Le mode DELIVERY_ASAP ne permet de réserver que dans les %d prochaines heures".formatted(maxHours),
                Map.of(
                        "mode", DeliveryMode.DELIVERY_ASAP.name(),
                        "maxAdvanceHours", maxHours,
                        "requestedSlotTime", slotDateTime.toString(),
                        "currentTime", currentTime.toString(),
                        "maxAllowedTime", currentTime.plusHours(maxHours).toString()
                )
        );
    }
}
