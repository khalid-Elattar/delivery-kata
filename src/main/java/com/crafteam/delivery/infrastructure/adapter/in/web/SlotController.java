package com.crafteam.delivery.infrastructure.adapter.in.web;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.application.port.in.CreateSlotUseCase;
import com.crafteam.delivery.application.port.in.GetAvailableSlotsUseCase;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.request.CreateSlotRequest;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.SlotResponse;
import com.crafteam.delivery.infrastructure.adapter.in.web.hateoas.SlotModelAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * REST controller for slot management.
 */
@RestController
@RequestMapping("/api/v1/slots")
@Tag(name = "Slots", description = "Delivery slot management API")
public class SlotController {

    private final CreateSlotUseCase createSlotUseCase;
    private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;
    private final SlotModelAssembler slotModelAssembler;

    public SlotController(CreateSlotUseCase createSlotUseCase,
                          GetAvailableSlotsUseCase getAvailableSlotsUseCase,
                          SlotModelAssembler slotModelAssembler) {
        this.createSlotUseCase = createSlotUseCase;
        this.getAvailableSlotsUseCase = getAvailableSlotsUseCase;
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
        CreateSlotCommand command = new CreateSlotCommand(
                request.deliveryMode(),
                request.date(),
                request.startTime(),
                request.endTime(),
                request.capacity()
        );

        return createSlotUseCase.execute(command)
                .map(SlotResponse::from)
                .map(slotModelAssembler::toModel);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get available slots",
            description = "Retrieves all available delivery slots for a specific mode and date"
    )
    @ApiResponse(responseCode = "200", description = "Slots retrieved successfully")
    public Mono<CollectionModel<EntityModel<SlotResponse>>> getAvailableSlots(
            @Parameter(description = "Delivery mode")
            @RequestParam DeliveryMode mode,
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return getAvailableSlotsUseCase.execute(mode, date)
                .map(SlotResponse::from)
                .map(slotModelAssembler::toModel)
                .collectList()
                .map(CollectionModel::of);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all slots", description = "Retrieves all delivery slots")
    public Mono<CollectionModel<EntityModel<SlotResponse>>> getAllSlots() {
        return getAvailableSlotsUseCase.executeAll()
                .map(SlotResponse::from)
                .map(slotModelAssembler::toModel)
                .collectList()
                .map(CollectionModel::of);
    }
}
