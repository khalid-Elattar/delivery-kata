package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalTime;

public class CutoffTimePassedException extends BookingValidationException {
    public CutoffTimePassedException(DeliveryMode mode, LocalTime cutoffTime) {
        super("CUTOFF_TIME_PASSED",
                String.format("Cutoff time %s has passed for %s", cutoffTime, mode));
    }
}
