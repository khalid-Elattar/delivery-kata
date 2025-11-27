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

        return slotRepository.findByDeliveryModeAndDate(mode, query.date())
                .map(slot -> mapToAvailableSlotInfo(slot, mode, now))
                .collectList()
                .map(slots -> buildResponse(mode, query.date(), slots, null))
                .doOnSuccess(response ->
                        log.info("Found {} slots for mode={}, date={}",
                                response.availableSlots().size(), query.mode(), query.date()));
    }

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
