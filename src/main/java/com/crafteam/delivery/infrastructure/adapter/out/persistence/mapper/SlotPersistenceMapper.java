package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.SlotEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Slot domain objects and SlotEntity persistence objects.
 */
@Component
public class SlotPersistenceMapper {

    public SlotEntity toEntity(Slot slot) {
        return new SlotEntity(
                slot.getId().value(),
                slot.getDeliveryMode().name(),
                slot.getDate(),
                slot.getTimeSlot().startTime(),
                slot.getTimeSlot().endTime(),
                slot.getCapacity(),
                slot.getBookedCount()
        );
    }

    public Slot toDomain(SlotEntity entity) {
        return Slot.reconstitute(
                SlotId.from(entity.getId()),
                DeliveryMode.valueOf(entity.getDeliveryMode()),
                entity.getDate(),
                new TimeSlot(entity.getStartTime(), entity.getEndTime()),
                entity.getCapacity(),
                entity.getBookedCount()
        );
    }
}
