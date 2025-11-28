package com.crafteam.delivery.domain.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class SlotInPastException extends BookingValidationException {
    public SlotInPastException(LocalDate date, LocalTime time) {
        super("SLOT_IN_PAST",
                String.format("Cannot book slot in the past: %s %s", date, time));
    }
}
