package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.SlotSuggestion;
import com.crafteam.delivery.domain.model.user.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain service responsible for finding valid slot suggestions.
 *
 * <p>This service ensures that all suggested slots respect the business rules
 * defined for each delivery mode. A suggestion that violates any rule is a bug.</p>
 *
 * <h2>Validation Rules Applied:</h2>
 * <ul>
 *   <li>Slot must have available capacity</li>
 *   <li>Slot must be in the future</li>
 *   <li>Day must be available for the delivery mode</li>
 *   <li>Time must be within the mode's operating hours</li>
 *   <li>Minimum advance time must be respected</li>
 *   <li>Maximum advance days must be respected</li>
 *   <li>DELIVERY_TODAY: Only today + cutoff time check</li>
 *   <li>DELIVERY_ASAP: Only today + 4h window check</li>
 *   <li>User must not have already booked this slot</li>
 *   <li>User must not exceed max active bookings</li>
 * </ul>
 */
public class SlotSuggestionService {

    private static final int DEFAULT_MAX_SUGGESTIONS = 5;
    private final int maxActiveBookings;

    public SlotSuggestionService(int maxActiveBookings) {
        this.maxActiveBookings = maxActiveBookings;
    }

    /**
     * Finds valid slot suggestions for a user when the requested slot is unavailable.
     *
     * @param requestedSlot The slot the user originally wanted to book
     * @param candidateSlots All slots to consider as alternatives
     * @param userBookings User's existing bookings (to check for duplicates and limits)
     * @param now Current date/time for validation
     * @param maxSuggestions Maximum number of suggestions to return
     * @return List of valid suggestions sorted by proximity score (best first)
     */
    public List<SlotSuggestion> findValidSuggestions(
            Slot requestedSlot,
            List<Slot> candidateSlots,
            UserId userId,
            List<Booking> userBookings,
            LocalDateTime now,
            int maxSuggestions) {

        // Pre-check: If user has reached max active bookings, no suggestions possible
        long activeBookingsCount = countActiveBookings(userBookings);
        if (activeBookingsCount >= maxActiveBookings) {
            return Collections.emptyList();
        }

        // Get set of slot IDs already booked by this user
        Set<SlotId> userBookedSlotIds = getUserBookedSlotIds(userBookings);

        DeliveryMode mode = requestedSlot.getDeliveryMode();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // For DELIVERY_TODAY, if cutoff time has passed, no suggestions possible
        if (mode == DeliveryMode.DELIVERY_TODAY && mode.isCutoffTimePassed(currentTime)) {
            return Collections.emptyList();
        }

        // TODO: Rebuild - isValidSuggestion always returns false now
        // Need to rebuild the entire suggestion logic for slot templates
        return Collections.emptyList();

        /* OLD CODE
        return candidateSlots.stream()
                // Exclude the requested slot itself
                .filter(slot -> !slot.getId().equals(requestedSlot.getId()))
                // Apply all business rules
                .filter(slot -> isValidSuggestion(slot, mode, userId, userBookedSlotIds, now, today, currentTime))
                // Create suggestions with scoring
                .map(slot -> SlotSuggestion.create(slot, requestedSlot, now))
                // Sort by proximity score (best first)
                .sorted()
                // Limit results
                .limit(maxSuggestions)
                .toList();
        */
    }

    /**
     * Overload with default max suggestions.
     */
    public List<SlotSuggestion> findValidSuggestions(
            Slot requestedSlot,
            List<Slot> candidateSlots,
            UserId userId,
            List<Booking> userBookings,
            LocalDateTime now) {
        return findValidSuggestions(requestedSlot, candidateSlots, userId, userBookings, now, DEFAULT_MAX_SUGGESTIONS);
    }

    /**
     * Validates if a slot is a valid suggestion according to all business rules.
     * TODO: Rebuild for slot templates - slot.getDate(), slot.getTimeSlot() no longer exist
     */
    private boolean isValidSuggestion(
            Slot slot,
            DeliveryMode mode,
            UserId userId,
            Set<SlotId> userBookedSlotIds,
            LocalDateTime now,
            LocalDate today,
            LocalTime currentTime) {

        // TODO: Rebuild - all the methods below use old Slot API
        return false;

        /* OLD CODE
        // Rule 1: Slot must have available capacity
        if (!slot.isAvailable()) {
            return false;
        }

        // Rule 2: Slot must be in the future
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());
        if (!slotDateTime.isAfter(now)) {
            return false;
        }

        // Rule 3: Day must be available for this delivery mode
        if (!mode.isAvailableFor(slot.getDate())) {
            return false;
        }

        // Rule 4: Time must be within operating hours
        if (!mode.isValidSlotTime(slot.getTimeSlot().startTime())) {
            return false;
        }

        // Rule 5: Minimum advance time must be respected
        if (!mode.meetsMinAdvanceTime(slotDateTime, now)) {
            return false;
        }

        // Rule 6: Maximum advance days must be respected
        if (!mode.meetsMaxAdvanceDays(slot.getDate(), today)) {
            return false;
        }

        // Rule 7: Mode-specific date validation (DELIVERY_TODAY/ASAP = today only)
        if (!mode.isValidDate(slot.getDate(), today)) {
            return false;
        }

        // Rule 8: DELIVERY_TODAY cutoff check (already checked globally, but double-check)
        if (mode == DeliveryMode.DELIVERY_TODAY && mode.isCutoffTimePassed(currentTime)) {
            return false;
        }

        // Rule 9: DELIVERY_ASAP window check (max 4 hours ahead)
        if (mode == DeliveryMode.DELIVERY_ASAP && !mode.meetsAsapWindow(slotDateTime, now)) {
            return false;
        }

        // Rule 10: User must not have already booked this slot
        if (userBookedSlotIds.contains(slot.getId())) {
            return false;
        }

        return true;
        */
    }

    /**
     * Counts active (non-cancelled) bookings.
     */
    private long countActiveBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
    }

    /**
     * Gets the set of slot IDs that the user has already booked (non-cancelled).
     */
    private Set<SlotId> getUserBookedSlotIds(List<Booking> userBookings) {
        return userBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(Booking::getSlotId)
                .collect(Collectors.toSet());
    }

    /**
     * Generates a list of reasons why no suggestions are available.
     * Useful for providing feedback to the user.
     */
    public List<String> explainNoSuggestions(
            DeliveryMode mode,
            LocalDateTime now,
            long userActiveBookings) {

        var reasons = new java.util.ArrayList<String>();

        // Max bookings reached
        if (userActiveBookings >= maxActiveBookings) {
            reasons.add("Vous avez atteint le maximum de %d réservations actives".formatted(maxActiveBookings));
            return reasons; // This is the primary blocker
        }

        LocalTime currentTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();

        // Mode-specific explanations
        switch (mode) {
            case DELIVERY_TODAY -> {
                reasons.add("Mode DELIVERY_TODAY : seuls les créneaux d'aujourd'hui sont valides");
                if (mode.isCutoffTimePassed(currentTime)) {
                    reasons.add("Heure actuelle : %s - après l'heure limite de %s".formatted(
                            currentTime, mode.getCutoffTime()));
                }
                reasons.add("Délai minimum requis : %d heures avant le créneau".formatted(
                        mode.getMinAdvanceHours()));
            }
            case DELIVERY_ASAP -> {
                reasons.add("Mode DELIVERY_ASAP : seuls les créneaux dans les %d prochaines heures sont valides".formatted(
                        mode.getMaxAdvanceHours()));
                reasons.add("Délai minimum requis : 30 minutes avant le créneau");
            }
            case DELIVERY -> {
                reasons.add("Mode DELIVERY : disponible uniquement du lundi au vendredi");
                reasons.add("Délai minimum requis : %d heures (%d jour)".formatted(
                        mode.getMinAdvanceHours(), mode.getMinAdvanceHours() / 24));
                reasons.add("Délai maximum : %d jours à l'avance".formatted(mode.getMaxAdvanceDays()));
            }
            case DRIVE -> {
                reasons.add("Mode DRIVE : disponible du lundi au samedi (pas le dimanche)");
                reasons.add("Délai minimum requis : %d heures avant le créneau".formatted(
                        mode.getMinAdvanceHours()));
                reasons.add("Délai maximum : %d jours à l'avance".formatted(mode.getMaxAdvanceDays()));
            }
        }

        return reasons;
    }

    /**
     * Gets a recommendation message when no suggestions are available.
     */
    public String getRecommendation(DeliveryMode currentMode) {
        return switch (currentMode) {
            case DELIVERY_TODAY ->
                    "Essayez le mode DELIVERY pour réserver pour les prochains jours ouvrés";
            case DELIVERY_ASAP ->
                    "Essayez le mode DELIVERY_TODAY pour un créneau plus tard dans la journée";
            case DELIVERY ->
                    "Essayez le mode DRIVE qui offre plus de flexibilité (disponible le samedi)";
            case DRIVE ->
                    "Essayez une autre date ou consultez les autres modes de livraison";
        };
    }

    public int getMaxActiveBookings() {
        return maxActiveBookings;
    }
}
