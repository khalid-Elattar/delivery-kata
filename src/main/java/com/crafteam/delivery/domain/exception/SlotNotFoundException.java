package com.crafteam.delivery.domain.exception;

/**
 * Exception thrown when a slot cannot be found.
 */
public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(String slotId) {
        super("Slot not found: " + slotId);
    }
}
