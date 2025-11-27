package com.crafteam.delivery.application.dto.command;

/**
 * Command for booking a delivery slot.
 */
public record BookSlotCommand(
        String slotId,
        String userId
) {
}
