package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.interfaces.rest.dto.request.CreateSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.SlotResponse;
import com.crafteam.delivery.interfaces.rest.mapper.SlotWebMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.HashSet;

/**
 * Test configuration to provide SlotWebMapper for WebFluxTest.
 */
@TestConfiguration
public class SlotWebMapperTestConfig {

    @Bean
    public SlotWebMapper slotWebMapper() {
        return new SlotWebMapper() {
            @Override
            public SlotResponse toResponse(Slot slot) {
                if (slot == null) {
                    return null;
                }
                return new SlotResponse(
                        slot.getId().value(),
                        slot.getDeliveryMode(),
                        slot.getAvailableDays(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        (int) slot.getSlotDurationMinutes(),
                        slot.getCapacity()
                );
            }

            @Override
            public CreateSlotCommand toCommand(CreateSlotRequest request) {
                if (request == null) {
                    return null;
                }
                return new CreateSlotCommand(
                        request.deliveryMode(),
                        request.availableDays(),
                        request.startTime(),
                        request.endTime(),
                        request.slotDuration(),
                        request.capacity()
                );
            }
        };
    }
}
