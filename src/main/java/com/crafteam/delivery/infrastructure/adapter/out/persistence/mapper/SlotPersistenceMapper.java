package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.SlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalTime;
import java.util.UUID;

/**
 * MapStruct mapper for converting between Slot domain objects and SlotEntity persistence objects.
 */
@Mapper(componentModel = "spring")
public interface SlotPersistenceMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "slotIdToUuid")
    @Mapping(target = "deliveryMode", source = "deliveryMode", qualifiedByName = "deliveryModeToString")
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "endTime", source = "timeSlot.endTime")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isNew", constant = "true")
    SlotEntity toEntity(Slot slot);

    default Slot toDomain(SlotEntity entity) {
        if (entity == null) {
            return null;
        }
        return Slot.reconstitute(
                SlotId.from(entity.getId()),
                DeliveryMode.valueOf(entity.getDeliveryMode()),
                entity.getDate(),
                new TimeSlot(entity.getStartTime(), entity.getEndTime()),
                entity.getCapacity(),
                entity.getBookedCount()
        );
    }

    @Named("slotIdToUuid")
    default UUID slotIdToUuid(SlotId slotId) {
        return slotId != null ? slotId.value() : null;
    }

    @Named("deliveryModeToString")
    default String deliveryModeToString(DeliveryMode deliveryMode) {
        return deliveryMode != null ? deliveryMode.name() : null;
    }
}
