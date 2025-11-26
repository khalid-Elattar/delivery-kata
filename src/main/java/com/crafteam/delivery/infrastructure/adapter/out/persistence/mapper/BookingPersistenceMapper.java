package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * MapStruct mapper for converting between Booking domain objects and BookingEntity persistence objects.
 */
@Mapper(componentModel = "spring")
public interface BookingPersistenceMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "bookingIdToUuid")
    @Mapping(target = "slotId", source = "slotId", qualifiedByName = "slotIdToUuid")
    @Mapping(target = "customerId", source = "customerId", qualifiedByName = "customerIdToUuid")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    BookingEntity toEntity(Booking booking);

    default Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }
        return Booking.reconstitute(
                BookingId.from(entity.getId()),
                SlotId.from(entity.getSlotId()),
                CustomerId.from(entity.getCustomerId()),
                BookingStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getCancelledAt()
        );
    }

    @Named("bookingIdToUuid")
    default UUID bookingIdToUuid(BookingId bookingId) {
        return bookingId != null ? bookingId.value() : null;
    }

    @Named("slotIdToUuid")
    default UUID slotIdToUuid(SlotId slotId) {
        return slotId != null ? slotId.value() : null;
    }

    @Named("customerIdToUuid")
    default UUID customerIdToUuid(CustomerId customerId) {
        return customerId != null ? customerId.value() : null;
    }

    @Named("statusToString")
    default String statusToString(BookingStatus status) {
        return status != null ? status.name() : null;
    }
}
