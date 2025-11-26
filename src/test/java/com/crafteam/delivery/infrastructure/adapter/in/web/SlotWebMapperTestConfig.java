package com.crafteam.delivery.infrastructure.adapter.in.web;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.request.CreateSlotRequest;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.SlotResponse;
import com.crafteam.delivery.infrastructure.adapter.in.web.mapper.SlotWebMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

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
                        slot.getId().toString(),
                        slot.getDeliveryMode(),
                        slot.getDate(),
                        slot.getTimeSlot().startTime(),
                        slot.getTimeSlot().endTime(),
                        slot.getCapacity(),
                        slot.getBookedCount(),
                        slot.remainingCapacity(),
                        slot.isAvailable()
                );
            }

            @Override
            public CreateSlotCommand toCommand(CreateSlotRequest request) {
                if (request == null) {
                    return null;
                }
                return new CreateSlotCommand(
                        request.deliveryMode(),
                        request.date(),
                        request.startTime(),
                        request.endTime(),
                        request.capacity()
                );
            }
        };
    }
}
