package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when a requested slot time is not a valid slot start time for the delivery mode.
 * This happens when the time doesn't align with the mode's slot grid.
 */
public class InvalidSlotTimeException extends BookingValidationException {

    public InvalidSlotTimeException(DeliveryMode mode, LocalTime requestedTime, List<LocalTime> validTimes) {
        super("INVALID_SLOT_TIME",
                String.format(
                        "Invalid slot time %s for mode %s. Time must align with slot grid. Valid times are: %s",
                        requestedTime,
                        mode.name(),
                        formatValidTimes(validTimes)
                ));
    }

    public InvalidSlotTimeException(DeliveryMode mode, LocalTime requestedTime) {
        super("INVALID_SLOT_TIME",
                String.format(
                        "Invalid slot time %s for mode %s. Time must align with slot grid (duration: %d minutes, start: %s, end: %s)",
                        requestedTime,
                        mode.name(),
                        mode.getSlotDuration().toMinutes(),
                        mode.getStartTime(),
                        mode.getEndTime()
                ));
    }

    private static String formatValidTimes(List<LocalTime> validTimes) {
        if (validTimes.size() <= 5) {
            return validTimes.stream()
                    .map(LocalTime::toString)
                    .collect(Collectors.joining(", "));
        } else {
            // Show first 3 and last 2 if there are too many
            String first = validTimes.stream()
                    .limit(3)
                    .map(LocalTime::toString)
                    .collect(Collectors.joining(", "));
            String last = validTimes.stream()
                    .skip(Math.max(0, validTimes.size() - 2))
                    .map(LocalTime::toString)
                    .collect(Collectors.joining(", "));
            return first + ", ... " + last;
        }
    }
}
