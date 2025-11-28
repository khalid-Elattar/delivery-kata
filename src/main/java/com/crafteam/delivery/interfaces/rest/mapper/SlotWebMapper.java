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
 * Updated for new slot template architecture.
 */
@Mapper(componentModel = "spring")
public interface SlotWebMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "slotIdToUuid")
    @Mapping(target = "slotDurationMinutes", expression = "java((int)slot.getSlotDuration().toMinutes())")
    SlotResponse toResponse(Slot slot);

    CreateSlotCommand toCommand(CreateSlotRequest request);

    @Named("slotIdToUuid")
    default java.util.UUID slotIdToUuid(SlotId slotId) {
        return slotId != null ? slotId.value() : null;
    }
}
