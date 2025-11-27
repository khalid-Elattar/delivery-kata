package com.crafteam.delivery.interfaces.rest.dto.response;

import com.crafteam.delivery.application.dto.response.BookingResult;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * REST API response for booking attempts.
 * Can contain either a successful booking or suggestions for alternatives.
 */
public record BookingResultResponse(
        String status,
        BookingInfo booking,
        RequestedSlotInfo requestedSlot,
        List<SuggestionInfo> suggestions,
        RulesInfo rules,
        String message,
        String recommendation,
        List<String> reasons
) {

    /**
     * Creates a response from the application layer BookingResult.
     */
    public static BookingResultResponse from(BookingResult result) {
        return new BookingResultResponse(
                result.status().name(),
                result.booking() != null ? BookingInfo.from(result.booking()) : null,
                result.requestedSlot() != null ? RequestedSlotInfo.from(result.requestedSlot()) : null,
                result.suggestions() != null ? result.suggestions().stream()
                        .map(SuggestionInfo::from)
                        .toList() : List.of(),
                result.rules() != null ? RulesInfo.from(result.rules()) : null,
                result.message(),
                result.recommendation(),
                result.reasons()
        );
    }

    /**
     * Information about a successful booking.
     */
    public record BookingInfo(
            String bookingId,
            String slotId,
            String userId,
            String status,
            String createdAt
    ) {
        public static BookingInfo from(com.crafteam.delivery.domain.model.booking.Booking booking) {
            return new BookingInfo(
                    booking.getId().toString(),
                    booking.getSlotId().toString(),
                    booking.getUserId().toString(),
                    booking.getStatus().name(),
                    booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : null
            );
        }
    }

    /**
     * Information about the requested slot.
     */
    public record RequestedSlotInfo(
            String slotId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String reason
    ) {
        public static RequestedSlotInfo from(BookingResult.RequestedSlotInfo info) {
            return new RequestedSlotInfo(
                    info.slotId(),
                    info.date(),
                    info.startTime(),
                    info.endTime(),
                    info.reason()
            );
        }
    }

    /**
     * Information about a suggested slot.
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
        public static SuggestionInfo from(BookingResult.SuggestionInfo info) {
            return new SuggestionInfo(
                    info.slotId(),
                    info.date(),
                    info.startTime(),
                    info.endTime(),
                    info.remainingCapacity(),
                    info.suggestionType(),
                    info.reason(),
                    info.validUntil(),
                    RulesValidation.from(info.rulesValidation())
            );
        }
    }

    /**
     * Validation status for rules.
     */
    public record RulesValidation(
            boolean minAdvanceRespected,
            boolean maxAdvanceRespected,
            boolean dayAvailable,
            boolean timeInRange
    ) {
        public static RulesValidation from(BookingResult.RulesValidation validation) {
            return new RulesValidation(
                    validation.minAdvanceRespected(),
                    validation.maxAdvanceRespected(),
                    validation.dayAvailable(),
                    validation.timeInRange()
            );
        }
    }

    /**
     * Business rules information.
     */
    public record RulesInfo(
            String mode,
            int minAdvanceHours,
            int maxAdvanceDays,
            List<String> availableDays,
            LocalTime startTime,
            LocalTime endTime
    ) {
        public static RulesInfo from(BookingResult.RulesInfo info) {
            return new RulesInfo(
                    info.mode().name(),
                    info.minAdvanceHours(),
                    info.maxAdvanceDays(),
                    info.availableDays(),
                    info.startTime(),
                    info.endTime()
            );
        }
    }
}
