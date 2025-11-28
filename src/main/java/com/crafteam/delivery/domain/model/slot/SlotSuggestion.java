package com.crafteam.delivery.domain.model.slot;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object representing a suggested alternative slot when the requested slot is unavailable.
 * Includes metadata about why this slot is suggested and its relevance score.
 *
 * <p>Suggestions are sorted by proximity score (higher is better), making it easy
 * to present the most relevant alternatives to the user.</p>
 *
 * TODO: DISABLED - Entire class needs rewrite for new Slot architecture
 * The old Slot had: date, timeSlot, bookedCount, remainingCapacity
 * The new Slot is a template with: availableDays, startTime, endTime, slotDuration, capacity
 *
 * New SlotSuggestion should contain:
 * - Slot template (reference to the delivery mode template)
 * - LocalDate (suggested date)
 * - LocalTime (suggested time)
 * - int remainingCapacity (calculated from bookings)
 * - SuggestionType, proximityScore, reason, validUntil (same as before)
 */
public record SlotSuggestion(
        Slot slot,
        SuggestionType type,
        int proximityScore,
        String reason,
        LocalDateTime validUntil
) implements Comparable<SlotSuggestion> {

    /**
     * Maximum proximity score for slots on the same day.
     */
    public static final int SAME_DAY_BONUS = 1000;

    /**
     * Bonus for matching the same time as requested.
     */
    public static final int SAME_TIME_BONUS = 500;

    public SlotSuggestion {
        Objects.requireNonNull(slot, "Slot is required");
        Objects.requireNonNull(type, "Suggestion type is required");
        Objects.requireNonNull(reason, "Reason is required");
        Objects.requireNonNull(validUntil, "Valid until is required");
    }

    // TODO: DISABLED - Needs rewrite for new Slot architecture
    // This factory method should take:
    // - Slot template
    // - LocalDate suggestedDate, LocalTime suggestedTime
    // - LocalDate requestedDate, LocalTime requestedTime
    // - int remainingCapacity
    // - LocalDateTime now
    /*
    public static SlotSuggestion create(Slot slot, Slot requestedSlot, LocalDateTime now) {
        SuggestionType type = determineSuggestionType(slot, requestedSlot);
        int score = calculateProximityScore(slot, requestedSlot);
        String reason = generateReason(slot, requestedSlot, type);
        LocalDateTime validUntil = calculateValidUntil(slot, now);

        return new SlotSuggestion(slot, type, score, reason, validUntil);
    }
    */

    // TODO: DISABLED - Uses old Slot API (getDate(), getTimeSlot())
    /*
    private static SuggestionType determineSuggestionType(Slot suggested, Slot requested) {
        boolean sameDay = suggested.getDate().equals(requested.getDate());
        boolean sameTime = suggested.getTimeSlot().startTime().equals(requested.getTimeSlot().startTime());

        if (sameDay) {
            if (suggested.getTimeSlot().startTime().isBefore(requested.getTimeSlot().startTime())) {
                return SuggestionType.SAME_DAY_EARLIER;
            }
            return SuggestionType.SAME_DAY_LATER;
        }

        if (suggested.getDate().equals(requested.getDate().plusDays(1)) && sameTime) {
            return SuggestionType.NEXT_DAY_SAME_TIME;
        }

        if (sameTime) {
            return SuggestionType.NEXT_AVAILABLE_SAME_TIME;
        }

        return SuggestionType.NEXT_AVAILABLE_DAY;
    }
    */

    // TODO: DISABLED - Uses old Slot API (getDate(), getTimeSlot(), remainingCapacity())
    /*
    private static int calculateProximityScore(Slot suggested, Slot requested) {
        int score = 0;

        // Same day is highly preferred
        if (suggested.getDate().equals(requested.getDate())) {
            score += SAME_DAY_BONUS;
        } else {
            // Decrease score based on days difference (max 100 points per day penalty)
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(
                    requested.getDate(), suggested.getDate());
            score -= Math.min(daysDifference * 100, 900);
        }

        // Same time is preferred
        if (suggested.getTimeSlot().startTime().equals(requested.getTimeSlot().startTime())) {
            score += SAME_TIME_BONUS;
        } else {
            // Decrease score based on hours difference
            long minutesDifference = Math.abs(
                    java.time.Duration.between(
                            suggested.getTimeSlot().startTime(),
                            requested.getTimeSlot().startTime()
                    ).toMinutes()
            );
            score -= Math.min(minutesDifference * 2, 400);
        }

        // Higher remaining capacity is slightly preferred
        score += Math.min(suggested.remainingCapacity() * 10, 50);

        return score;
    }
    */

    // TODO: DISABLED - Uses old Slot API (getDate(), getTimeSlot())
    /*
    private static String generateReason(Slot suggested, Slot requested, SuggestionType type) {
        return switch (type) {
            case SAME_DAY_EARLIER -> "Même jour, %d minutes plus tôt".formatted(
                    java.time.Duration.between(
                            suggested.getTimeSlot().startTime(),
                            requested.getTimeSlot().startTime()
                    ).toMinutes()
            );
            case SAME_DAY_LATER -> "Même jour, %d minutes plus tard".formatted(
                    java.time.Duration.between(
                            requested.getTimeSlot().startTime(),
                            suggested.getTimeSlot().startTime()
                    ).toMinutes()
            );
            case NEXT_DAY_SAME_TIME -> "Lendemain, même heure (%s)".formatted(
                    suggested.getTimeSlot().startTime()
            );
            case NEXT_AVAILABLE_SAME_TIME -> "Le %s, même heure (%s)".formatted(
                    suggested.getDate(),
                    suggested.getTimeSlot().startTime()
            );
            case NEXT_AVAILABLE_DAY -> "Le %s à %s".formatted(
                    suggested.getDate(),
                    suggested.getTimeSlot().startTime()
            );
            case CLOSEST_AVAILABLE -> "Créneau le plus proche : %s à %s".formatted(
                    suggested.getDate(),
                    suggested.getTimeSlot().startTime()
            );
        };
    }
    */

    // TODO: DISABLED - Uses old Slot API (getDate(), getTimeSlot())
    /*
    private static LocalDateTime calculateValidUntil(Slot slot, LocalDateTime now) {
        DeliveryMode mode = slot.getDeliveryMode();
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());

        // Calculate when the minimum advance time will no longer be met
        return switch (mode) {
            case DELIVERY_ASAP -> slotDateTime.minusMinutes(30);
            case DELIVERY_TODAY -> {
                LocalDateTime cutoffLimit = LocalDateTime.of(slot.getDate(), mode.getCutoffTime());
                LocalDateTime advanceLimit = slotDateTime.minusHours(mode.getMinAdvanceHours());
                yield cutoffLimit.isBefore(advanceLimit) ? cutoffLimit : advanceLimit;
            }
            default -> slotDateTime.minusHours(mode.getMinAdvanceHours());
        };
    }
    */

    /**
     * Compares suggestions by proximity score (descending order).
     * Higher scores come first.
     */
    @Override
    public int compareTo(SlotSuggestion other) {
        return Integer.compare(other.proximityScore, this.proximityScore);
    }

    /**
     * Returns the slot ID for convenience.
     */
    public SlotId getSlotId() {
        return slot.getId();
    }

    // TODO: DISABLED - Uses old Slot API (remainingCapacity())
    // In new architecture, remaining capacity needs to be calculated from:
    // slot.getCapacity() - currentBookingCount (from repository)
    /*
    public int getRemainingCapacity() {
        return slot.remainingCapacity();
    }
    */

    /**
     * Checks if this suggestion is still valid at the given time.
     */
    public boolean isValidAt(LocalDateTime time) {
        return time.isBefore(validUntil);
    }
}
