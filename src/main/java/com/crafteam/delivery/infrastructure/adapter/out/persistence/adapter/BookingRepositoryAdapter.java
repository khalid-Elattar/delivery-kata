package com.crafteam.delivery.infrastructure.adapter.out.persistence.adapter;

import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.BookingPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.BookingR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing BookingRepository port using R2DBC.
 */
@Component
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingR2dbcRepository r2dbcRepository;
    private final BookingPersistenceMapper mapper;

    public BookingRepositoryAdapter(BookingR2dbcRepository r2dbcRepository, BookingPersistenceMapper mapper) {
        this.r2dbcRepository = r2dbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Booking> findById(BookingId id) {
        return r2dbcRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Booking> findByCustomerId(CustomerId customerId) {
        return r2dbcRepository.findByCustomerId(customerId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Booking> findBySlotId(SlotId slotId) {
        return r2dbcRepository.findBySlotId(slotId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Booking> save(Booking booking) {
        return r2dbcRepository.save(mapper.toEntity(booking))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(BookingId id) {
        return r2dbcRepository.deleteById(id.value());
    }
}
