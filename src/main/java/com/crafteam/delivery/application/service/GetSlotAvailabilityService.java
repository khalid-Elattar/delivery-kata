package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.query.SlotAvailabilityQuery;
import com.crafteam.delivery.application.dto.response.SlotAvailabilityResponse;
import com.crafteam.delivery.application.dto.response.SlotAvailabilityResponse.AvailableSlotInfo;
import com.crafteam.delivery.application.dto.response.SlotAvailabilityResponse.RulesInfo;
import com.crafteam.delivery.application.port.in.GetSlotAvailabilityUseCase;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for retrieving slot availability with business rules validation.
 */
@Service
public class GetSlotAvailabilityService implements GetSlotAvailabilityUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSlotAvailabilityService.class);

    private final SlotRepository slotRepository;
    private final Clock clock;

    public GetSlotAvailabilityService(SlotRepository slotRepository, Clock clock) {
        this.slotRepository = slotRepository;
        this.clock = clock;
    }

    @Override
    public Mono<SlotAvailabilityResponse> execute(SlotAvailabilityQuery query) {
        log.info("Getting slot availability for mode={}, date={}", query.mode(), query.date());

        // TODO: DISABLED - Needs complete rewrite for new Slot architecture
        // The new architecture:
        // 1. Slots are templates (no specific date/time, just availability rules)
        // 2. Bookings reference slot templates + specify date/time
        // 3. To get availability, we need to:
        //    a) Find slot template for the delivery mode
        //    b) Generate valid time slots for the requested date (based on template rules)
        //    c) Query bookings for that date/time to calculate remaining capacity
        //    d) Apply business rules (min advance, max advance, day validation, etc.)
        //
        // Implementation approach:
        // - SlotRepository.findByDeliveryMode(mode) -> get template
        // - template.getValidBookingTimes() -> get possible time slots
        // - BookingRepository.countByDateTimeAndMode() -> get current bookings per slot
        // - Apply capacity and business rules to determine availability

        DeliveryMode mode = query.mode();
        LocalDateTime now = LocalDateTime.now(clock);

        // First validate if the date is valid for this mode
        DeliveryMode.ValidationResult dateValidation = mode.validateBookingTime(
                LocalDateTime.of(query.date(), mode.getStartTime()),
                now
        );

        if (!dateValidation.valid() &&
                ("INVALID_DAY".equals(dateValidation.errorCode()) ||
                        "INVALID_DATE".equals(dateValidation.errorCode()))) {
            // Date is not valid for this mode, return empty slots with explanation
            return Mono.just(buildResponse(mode, query.date(), List.of(), dateValidation.message()));
        }

        // TEMPORARY: Return empty response until properly implemented
        log.warn("GetSlotAvailabilityService is disabled - needs rewrite for new architecture");
        return Mono.just(buildResponse(mode, query.date(), List.of(),
                "Service temporarily disabled - slot availability needs architectural update"));
    }

    // TODO: DISABLED - mapToAvailableSlotInfo needs rewrite
    // This method was using old Slot API (getDate, getTimeSlot, isAvailable, remainingCapacity)
    // New implementation should:
    // 1. Take (Slot template, LocalDate, LocalTime, current booking count)
    // 2. Calculate remaining capacity = template.capacity - bookingCount
    // 3. Validate booking time rules
    // 4. Return AvailableSlotInfo
    /*
    private AvailableSlotInfo mapToAvailableSlotInfo(Slot slot, DeliveryMode mode, LocalDateTime now) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().startTime());

        // Check if slot is available based on capacity
        if (!slot.isAvailable()) {
            return AvailableSlotInfo.unavailable(
                    slot.getId().value().toString(),
                    slot.getTimeSlot().startTime(),
                    slot.getTimeSlot().endTime(),
                    "FULLY_BOOKED"
            );
        }

        // Check if booking time constraints are met
        DeliveryMode.ValidationResult validation = mode.validateBookingTime(slotDateTime, now);

        if (!validation.valid()) {
            return AvailableSlotInfo.unavailable(
                    slot.getId().value().toString(),
                    slot.getTimeSlot().startTime(),
                    slot.getTimeSlot().endTime(),
                    validation.errorCode()
            );
        }

        return AvailableSlotInfo.available(
                slot.getId().value().toString(),
                slot.getTimeSlot().startTime(),
                slot.getTimeSlot().endTime(),
                slot.remainingCapacity()
        );
    }
    */

    private SlotAvailabilityResponse buildResponse(DeliveryMode mode, java.time.LocalDate date,
                                                   List<AvailableSlotInfo> slots, String dateError) {
        RulesInfo rules = new RulesInfo(
                mode.getMinAdvanceHours(),
                mode.getMaxAdvanceDays(),
                mode.getSlotDuration(),
                mode.getStartTime(),
                mode.getEndTime(),
                mode.getDefaultCapacity()
        );

        // If there's a date error, add it as a single unavailable slot indicator
        if (dateError != null && slots.isEmpty()) {
            log.warn("Date {} not available for mode {}: {}", date, mode, dateError);
        }

        return new SlotAvailabilityResponse(mode, date, slots, rules);
    }
}
