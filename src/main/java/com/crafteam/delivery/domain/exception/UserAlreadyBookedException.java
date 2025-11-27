package com.crafteam.delivery.domain.exception;

import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;

import java.util.Map;

/**
 * Exception thrown when a user tries to book the same slot twice.
 */
public class UserAlreadyBookedException extends BookingValidationException {

    public UserAlreadyBookedException(UserId userId, SlotId slotId) {
        super(
                "USER_ALREADY_BOOKED",
                "L'utilisateur a déjà une réservation pour ce créneau",
                Map.of(
                        "userId", userId.value().toString(),
                        "slotId", slotId.value().toString()
                )
        );
    }
}
