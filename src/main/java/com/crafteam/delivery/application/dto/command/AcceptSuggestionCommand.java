package com.crafteam.delivery.application.dto.command;

/**
 * Command for accepting a suggested slot.
 */
public record AcceptSuggestionCommand(
        String slotId,
        String userId,
        String originalSlotId
) {
}
