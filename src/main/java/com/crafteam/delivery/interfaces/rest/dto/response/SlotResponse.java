package com.crafteam.delivery.interfaces.rest.dto.response;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record SlotResponse(
        UUID id,
        DeliveryMode deliveryMode,
        Set<DayOfWeek> availableDays,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        int capacity
) {}
