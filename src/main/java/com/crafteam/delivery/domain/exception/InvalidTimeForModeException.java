package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalTime;
import java.util.List;

public class InvalidTimeForModeException extends BookingValidationException {
    public InvalidTimeForModeException(DeliveryMode mode, LocalTime time,
                                       LocalTime start, LocalTime end, List<LocalTime> validTimes) {
        super("INVALID_TIME_FOR_MODE",
                String.format("Time %s is not valid for %s (hours: %s-%s). Valid times: %s",
                        time, mode, start, end, validTimes));
    }
}
