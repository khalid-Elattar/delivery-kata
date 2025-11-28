package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class InvalidDateForModeException extends BookingValidationException {
    public InvalidDateForModeException(DeliveryMode mode, LocalDate date, Set<DayOfWeek> validDays) {
        super("INVALID_DATE_FOR_MODE",
                String.format("Date %s (%s) is not valid for %s. Valid days: %s",
                        date, date.getDayOfWeek(), mode, validDays));
    }

    public InvalidDateForModeException(DeliveryMode mode, LocalDate date, String reason) {
        super("INVALID_DATE_FOR_MODE",
                String.format("Date %s is not valid for %s: %s", date, mode, reason));
    }
}
