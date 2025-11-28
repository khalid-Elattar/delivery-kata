package com.crafteam.delivery.application.dto.response;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotSuggestion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Result of a booking attempt.
 *
 * <p>Can represent either:
 * <ul>
 *   <li>A successful booking (status = CONFIRMED)</li>
 *   <li>An unavailable slot with suggestions (status = SLOT_UNAVAILABLE)</li>
 *   <li>No alternatives available (status = NO_ALTERNATIVES)</li>
 *   <li>User has reached max bookings (status = MAX_BOOKINGS_REACHED)</li>
 * </ul>
 */
public record BookingResult(
        BookingResultStatus status,
        Booking booking,
        RequestedSlotInfo requestedSlot,
        List<SuggestionInfo> suggestions,
        RulesInfo rules,
        String message,
        String recommendation,
        List<String> reasons
) {

    /**
     * Creates a successful booking result.
     */
    public static BookingResult confirmed(Booking booking, Slot slotTemplate) {
        Objects.requireNonNull(booking, "Booking is required");
        Objects.requireNonNull(slotTemplate, "Slot template is required");

        return new BookingResult(
                BookingResultStatus.CONFIRMED,
                booking,
                RequestedSlotInfo.from(slotTemplate, booking.getBookingDate(), booking.getBookingTime(), null),
                List.of(),
                RulesInfo.from(slotTemplate.getDeliveryMode()),
                "Réservation confirmée",
                null,
                List.of()
        );
    }

    /**
     * Creates a result for unavailable slot with suggestions.
     *
     * TODO: DISABLED - Needs rewrite for new Slot architecture
     * This method is commented out because:
     * 1. SlotSuggestion uses old Slot API
     * 2. SuggestionInfo.from() is disabled
     * 3. Entire suggestion system needs redesign
     */
    /*
    public static BookingResult unavailableWithSuggestions(
            Slot requestedSlot,
            List<SlotSuggestion> suggestions,
            String unavailabilityReason) {

        Objects.requireNonNull(requestedSlot, "Requested slot is required");
        Objects.requireNonNull(suggestions, "Suggestions list is required");

        List<SuggestionInfo> suggestionInfos = suggestions.stream()
                .map(SuggestionInfo::from)
                .toList();

        return new BookingResult(
                BookingResultStatus.SLOT_UNAVAILABLE,
                null,
                RequestedSlotInfo.from(requestedSlot, unavailabilityReason),
                suggestionInfos,
                RulesInfo.from(requestedSlot.getDeliveryMode()),
                "Ce créneau est complet. Voici des alternatives disponibles :",
                null,
                List.of()
        );
    }
    */

    /**
     * Creates a result when no alternatives are available.
     *
     * TODO: DISABLED - Needs rewrite for new Slot architecture
     * This method needs: (Slot template, LocalDate, LocalTime, unavailabilityReason, reasons, recommendation)
     */
    /*
    public static BookingResult noAlternatives(
            Slot requestedSlot,
            String unavailabilityReason,
            List<String> reasons,
            String recommendation) {

        Objects.requireNonNull(requestedSlot, "Requested slot is required");

        return new BookingResult(
                BookingResultStatus.NO_ALTERNATIVES,
                null,
                RequestedSlotInfo.from(requestedSlot, unavailabilityReason),
                List.of(),
                RulesInfo.from(requestedSlot.getDeliveryMode()),
                "Aucun créneau alternatif disponible pour ce mode de livraison",
                recommendation,
                reasons != null ? reasons : List.of()
        );
    }
    */

    /**
     * Creates a result when user has reached max bookings.
     *
     * TODO: DISABLED - Needs rewrite for new Slot architecture
     * This method needs: (Slot template, LocalDate, LocalTime, currentBookings, maxBookings)
     */
    /*
    public static BookingResult maxBookingsReached(
            Slot requestedSlot,
            int currentBookings,
            int maxBookings) {

        Objects.requireNonNull(requestedSlot, "Requested slot is required");

        return new BookingResult(
                BookingResultStatus.MAX_BOOKINGS_REACHED,
                null,
                RequestedSlotInfo.from(requestedSlot, "USER_MAX_BOOKINGS"),
                List.of(),
                RulesInfo.from(requestedSlot.getDeliveryMode()),
                "Vous avez atteint le maximum de %d réservations actives".formatted(maxBookings),
                "Annulez une réservation existante pour pouvoir en créer une nouvelle",
                List.of("Nombre de réservations actives : %d/%d".formatted(currentBookings, maxBookings))
        );
    }
    */

    /**
     * Checks if the booking was successful.
     */
    public boolean isSuccessful() {
        return status == BookingResultStatus.CONFIRMED;
    }

    /**
     * Checks if suggestions are available.
     */
    public boolean hasSuggestions() {
        return suggestions != null && !suggestions.isEmpty();
    }

    /**
     * Status of the booking attempt.
     */
    public enum BookingResultStatus {
        CONFIRMED,
        SLOT_UNAVAILABLE,
        NO_ALTERNATIVES,
        MAX_BOOKINGS_REACHED
    }

    /**
     * Information about the originally requested slot.
     */
    public record RequestedSlotInfo(
            String slotId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String reason
    ) {
        // TODO: DISABLED - Needs rewrite for new Slot architecture
        // Old Slot had: getDate(), getTimeSlot()
        // New Slot is a template with: availableDays, startTime, endTime, slotDuration
        // This factory method should take: (Slot template, LocalDate, LocalTime, reason)
        // For now, create RequestedSlotInfo directly from booking data instead
        /*
        public static RequestedSlotInfo from(Slot slot, String reason) {
            return new RequestedSlotInfo(
                    slot.getId().value().toString(),
                    slot.getDate(),
                    slot.getTimeSlot().startTime(),
                    slot.getTimeSlot().endTime(),
                    reason
            );
        }
        */

        public static RequestedSlotInfo from(Slot slotTemplate, LocalDate date, LocalTime startTime, String reason) {
            LocalTime endTime = slotTemplate.calculateEndTime(startTime);
            return new RequestedSlotInfo(
                    slotTemplate.getId().value().toString(),
                    date,
                    startTime,
                    endTime,
                    reason
            );
        }
    }

    /**
     * Information about a suggested alternative slot.
     */
    public record SuggestionInfo(
            String slotId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int remainingCapacity,
            String suggestionType,
            String reason,
            LocalDateTime validUntil,
            RulesValidation rulesValidation
    ) {
        // TODO: DISABLED - Needs rewrite for new Slot architecture
        // SlotSuggestion is also using old Slot API (slot.getDate(), slot.getTimeSlot())
        // This entire suggestion system needs architectural redesign
        /*
        public static SuggestionInfo from(SlotSuggestion suggestion) {
            return new SuggestionInfo(
                    suggestion.getSlotId().value().toString(),
                    suggestion.slot().getDate(),
                    suggestion.slot().getTimeSlot().startTime(),
                    suggestion.slot().getTimeSlot().endTime(),
                    suggestion.getRemainingCapacity(),
                    suggestion.type().name(),
                    suggestion.reason(),
                    suggestion.validUntil(),
                    new RulesValidation(true, true, true, true)
            );
        }
        */
    }

    /**
     * Validation status for business rules.
     */
    public record RulesValidation(
            boolean minAdvanceRespected,
            boolean maxAdvanceRespected,
            boolean dayAvailable,
            boolean timeInRange
    ) {}

    /**
     * Business rules for the delivery mode.
     */
    public record RulesInfo(
            DeliveryMode mode,
            int minAdvanceHours,
            int maxAdvanceDays,
            List<String> availableDays,
            LocalTime startTime,
            LocalTime endTime
    ) {
        public static RulesInfo from(DeliveryMode mode) {
            List<String> days = mode.getAvailableDays().stream()
                    .map(Enum::name)
                    .sorted()
                    .toList();

            return new RulesInfo(
                    mode,
                    mode.getMinAdvanceHours(),
                    mode.getMaxAdvanceDays(),
                    days,
                    mode.getStartTime(),
                    mode.getEndTime()
            );
        }
    }
}
