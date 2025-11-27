package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.user.UserId;

import java.util.Map;

/**
 * Exception thrown when a user exceeds the maximum number of active bookings.
 */
public class MaxActiveBookingsException extends BookingValidationException {

    private static final int DEFAULT_MAX_ACTIVE_BOOKINGS = 3;

    public MaxActiveBookingsException(UserId userId, int currentActiveCount) {
        this(userId, currentActiveCount, DEFAULT_MAX_ACTIVE_BOOKINGS);
    }

    public MaxActiveBookingsException(UserId userId, int currentActiveCount, int maxAllowed) {
        super(
                "MAX_ACTIVE_BOOKINGS_EXCEEDED",
                "L'utilisateur ne peut pas avoir plus de %d réservations actives simultanément".formatted(maxAllowed),
                Map.of(
                        "userId", userId.value().toString(),
                        "currentActiveBookings", currentActiveCount,
                        "maxAllowed", maxAllowed
                )
        );
    }
}
