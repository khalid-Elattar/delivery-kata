package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.SlotEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Slot domain objects and SlotEntity persistence objects.
 * Handles the new slot template structure with available days and duration.
 */
@Component
public class SlotPersistenceMapper {

    /**
     * Convert domain Slot to persistence SlotEntity.
     */
    public SlotEntity toEntity(Slot slot) {
        return SlotEntity.builder()
                .id(slot.getId().value())
                .deliveryMode(slot.getDeliveryMode().name())
                .availableDays(daysToString(slot.getAvailableDays()))
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotDuration((int) slot.getSlotDuration().toMinutes())
                .capacity(slot.getCapacity())
                .createdAt(Instant.now())
                .isNew(true)
                .build();
    }

    /**
     * Convert persistence SlotEntity to domain Slot.
     */
    public Slot toDomain(SlotEntity entity) {
        if (entity == null) {
            return null;
        }

        return Slot.reconstitute(
                SlotId.from(entity.getId()),
                DeliveryMode.valueOf(entity.getDeliveryMode()),
                stringToDays(entity.getAvailableDays()),
                entity.getStartTime(),
                entity.getEndTime(),
                Duration.ofMinutes(entity.getSlotDuration()),
                entity.getCapacity()
        );
    }

    /**
     * Convert Set<DayOfWeek> to comma-separated string.
     */
    private String daysToString(Set<DayOfWeek> days) {
        return days.stream()
                .map(DayOfWeek::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * Convert comma-separated string to Set<DayOfWeek>.
     */
    private Set<DayOfWeek> stringToDays(String daysString) {
        return Arrays.stream(daysString.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }
}
