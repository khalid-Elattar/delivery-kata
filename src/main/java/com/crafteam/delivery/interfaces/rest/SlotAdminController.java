package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.interfaces.rest.dto.request.CreateSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.SlotResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/slots")
@PreAuthorize("hasRole('ADMIN')")
public class SlotAdminController {

    private final SlotRepository slotRepository;

    public SlotAdminController(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        // Convert list to set
        Set<DayOfWeek> availableDays = Set.copyOf(request.availableDays());

        // Create slot template
        Slot slot = Slot.create(
                request.deliveryMode(),
                availableDays,
                request.startTime(),
                request.endTime(),
                Duration.ofMinutes(request.slotDuration()),
                request.capacity()
        );

        return slotRepository.save(slot)
                .map(this::toResponse);
    }

    @GetMapping
    public Flux<SlotResponse> getAllSlots() {
        return slotRepository.findAll()
                .map(this::toResponse);
    }

    @GetMapping("/{deliveryMode}")
    public Mono<SlotResponse> getSlotByMode(@PathVariable String deliveryMode) {
        return slotRepository.findByDeliveryMode(
                        com.crafteam.delivery.domain.model.slot.DeliveryMode.valueOf(deliveryMode)
                )
                .map(this::toResponse);
    }

    private SlotResponse toResponse(Slot slot) {
        return new SlotResponse(
                slot.getId().value(),
                slot.getDeliveryMode(),
                slot.getAvailableDays(),
                slot.getStartTime(),
                slot.getEndTime(),
                (int) slot.getSlotDuration().toMinutes(),
                slot.getCapacity()
        );
    }
}
