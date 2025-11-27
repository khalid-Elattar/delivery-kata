package com.crafteam.delivery.domain.model.slot;

/**
 * Represents the type of slot suggestion offered when the originally requested slot is unavailable.
 * Each type indicates the relationship between the suggested slot and the requested slot.
 */
public enum SuggestionType {

    /**
     * Same day, earlier time slot.
     */
    SAME_DAY_EARLIER("Même jour, plus tôt"),

    /**
     * Same day, later time slot.
     */
    SAME_DAY_LATER("Même jour, plus tard"),

    /**
     * Next day, same time slot.
     */
    NEXT_DAY_SAME_TIME("Jour suivant, même heure"),

    /**
     * Next available day with same time.
     */
    NEXT_AVAILABLE_SAME_TIME("Prochain jour disponible, même heure"),

    /**
     * Next available day with different time.
     */
    NEXT_AVAILABLE_DAY("Prochain jour disponible"),

    /**
     * Closest available slot overall.
     */
    CLOSEST_AVAILABLE("Créneau le plus proche disponible");

    private final String description;

    SuggestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
