package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.BookingStatus;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Booking domain objects and BookingEntity persistence objects.
 */
@Component
public class BookingPersistenceMapper {

    public BookingEntity toEntity(Booking booking) {
        return new BookingEntity(
                booking.getId().value(),
                booking.getSlotId().value(),
                booking.getCustomerId().value(),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                booking.getConfirmedAt(),
                booking.getCancelledAt()
        );
    }

    public Booking toDomain(BookingEntity entity) {
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
}
