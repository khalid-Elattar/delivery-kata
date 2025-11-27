package com.crafteam.delivery.domain.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Exception thrown when trying to book a slot that is in the past.
 */
public class SlotInPastException extends BookingValidationException {

    public SlotInPastException(LocalDateTime slotDateTime, LocalDateTime currentTime) {
        super(
                "SLOT_IN_PAST",
                "Impossible de réserver un créneau dans le passé",
                Map.of(
                        "requestedSlotTime", slotDateTime.toString(),
                        "currentTime", currentTime.toString()
                )
        );
    }
}
