package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.query.SlotAvailabilityQuery;
import com.crafteam.delivery.application.dto.response.SlotAvailabilityResponse;
import com.crafteam.delivery.application.port.in.CreateSlotUseCase;
import com.crafteam.delivery.application.port.in.GetAvailableSlotsUseCase;
import com.crafteam.delivery.application.port.in.GetSlotAvailabilityUseCase;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.interfaces.rest.dto.request.CreateSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.SlotResponse;
import com.crafteam.delivery.interfaces.rest.hateoas.SlotModelAssembler;
import com.crafteam.delivery.interfaces.rest.mapper.SlotWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for slot management.
 */
@RestController
@RequestMapping("/api/v1/slots")
@Tag(name = "Slots", description = "Delivery slot management API")
public class SlotController {

    private final CreateSlotUseCase createSlotUseCase;
    private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;
    private final GetSlotAvailabilityUseCase getSlotAvailabilityUseCase;
    private final SlotWebMapper slotWebMapper;
    private final SlotModelAssembler slotModelAssembler;

    public SlotController(CreateSlotUseCase createSlotUseCase,
                          GetAvailableSlotsUseCase getAvailableSlotsUseCase,
                          GetSlotAvailabilityUseCase getSlotAvailabilityUseCase,
                          SlotWebMapper slotWebMapper,
                          SlotModelAssembler slotModelAssembler) {
        this.createSlotUseCase = createSlotUseCase;
        this.getAvailableSlotsUseCase = getAvailableSlotsUseCase;
        this.getSlotAvailabilityUseCase = getSlotAvailabilityUseCase;
        this.slotWebMapper = slotWebMapper;
        this.slotModelAssembler = slotModelAssembler;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new delivery slot",
            description = "Creates a new delivery slot with the specified delivery mode, date, and time range"
    )
    @ApiResponse(responseCode = "201", description = "Slot created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public Mono<EntityModel<SlotResponse>> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        return createSlotUseCase.execute(slotWebMapper.toCommand(request))
                .map(slotWebMapper::toResponse)
                .map(slotModelAssembler::toModel);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get available slots",
            description = "Retrieves all available delivery slots for a specific mode and date"
    )
    @ApiResponse(responseCode = "200", description = "Slots retrieved successfully")
    public Mono<List<SlotResponse>> getAvailableSlots(
            @Parameter(description = "Delivery mode")
            @RequestParam DeliveryMode mode,
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return getAvailableSlotsUseCase.execute(mode, date)
                .map(slotWebMapper::toResponse)
                .collectList();
    }

    @GetMapping(value = "/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get slot availability with business rules",
            description = """
                    Retrieves slot availability for a specific mode and date, including:
                    - Available and unavailable slots with reasons
                    - Business rules for the delivery mode (min advance, max advance, slot duration)
                    - Remaining capacity per slot

                    Unavailability reasons:
                    - FULLY_BOOKED: Slot is at maximum capacity
                    - MIN_ADVANCE_TIME_NOT_MET: Not enough advance time for booking
                    - CUTOFF_TIME_PASSED: Cutoff time has passed (DELIVERY_TODAY only)
                    - ASAP_WINDOW_EXCEEDED: Outside 4-hour window (DELIVERY_ASAP only)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Availability retrieved successfully",
            content = @Content(schema = @Schema(implementation = SlotAvailabilityResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid delivery mode or date")
    public Mono<SlotAvailabilityResponse> getSlotAvailability(
            @Parameter(description = "Delivery mode (DRIVE, DELIVERY, DELIVERY_TODAY, DELIVERY_ASAP)")
            @RequestParam DeliveryMode mode,
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        SlotAvailabilityQuery query = new SlotAvailabilityQuery(mode, date);
        return getSlotAvailabilityUseCase.execute(query);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all slots", description = "Retrieves all delivery slots with HATEOAS links")
    public Flux<EntityModel<SlotResponse>> getAllSlots() {
        return getAvailableSlotsUseCase.executeAll()
                .map(slotWebMapper::toResponse)
                .map(slotModelAssembler::toModel);
    }
}
