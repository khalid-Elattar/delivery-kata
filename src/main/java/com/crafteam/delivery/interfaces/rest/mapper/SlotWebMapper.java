package com.crafteam.delivery.interfaces.rest.mapper;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.interfaces.rest.dto.request.CreateSlotRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.SlotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for web layer slot conversions.
 */
@Mapper(componentModel = "spring")
public interface SlotWebMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "slotIdToString")
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "endTime", source = "timeSlot.endTime")
    @Mapping(target = "remainingCapacity", expression = "java(slot.remainingCapacity())")
    @Mapping(target = "available", expression = "java(slot.isAvailable())")
    SlotResponse toResponse(Slot slot);

    CreateSlotCommand toCommand(CreateSlotRequest request);

    @Named("slotIdToString")
    default String slotIdToString(SlotId slotId) {
        return slotId != null ? slotId.toString() : null;
    }
}
