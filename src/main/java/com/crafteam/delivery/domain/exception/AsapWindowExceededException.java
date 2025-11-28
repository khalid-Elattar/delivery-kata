package com.crafteam.delivery.domain.exception;

import java.time.LocalTime;

public class AsapWindowExceededException extends BookingValidationException {
    public AsapWindowExceededException(LocalTime bookingTime, LocalTime currentTime) {
        super("ASAP_WINDOW_EXCEEDED",
                String.format("ASAP booking at %s exceeds 4-hour window from %s",
                        bookingTime, currentTime));
    }
}
