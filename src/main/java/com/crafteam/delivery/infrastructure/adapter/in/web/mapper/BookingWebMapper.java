package com.crafteam.delivery.infrastructure.adapter.in.web.mapper;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.request.BookSlotRequest;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for web layer booking conversions.
 */
@Mapper(componentModel = "spring")
public interface BookingWebMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "bookingIdToString")
    @Mapping(target = "slotId", source = "slotId", qualifiedByName = "slotIdToString")
    @Mapping(target = "customerId", source = "customerId", qualifiedByName = "customerIdToString")
    BookingResponse toResponse(Booking booking);

    BookSlotCommand toCommand(BookSlotRequest request);

    @Named("bookingIdToString")
    default String bookingIdToString(BookingId bookingId) {
        return bookingId != null ? bookingId.toString() : null;
    }

    @Named("slotIdToString")
    default String slotIdToString(SlotId slotId) {
        return slotId != null ? slotId.toString() : null;
    }

    @Named("customerIdToString")
    default String customerIdToString(CustomerId customerId) {
        return customerId != null ? customerId.toString() : null;
    }
}
