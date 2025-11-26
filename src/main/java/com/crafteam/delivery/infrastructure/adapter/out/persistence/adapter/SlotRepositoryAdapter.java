package com.crafteam.delivery.infrastructure.adapter.out.persistence.adapter;

import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.SlotPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.SlotR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Adapter implementing SlotRepository port using R2DBC.
 */
@Component
public class SlotRepositoryAdapter implements SlotRepository {

    private final SlotR2dbcRepository r2dbcRepository;
    private final SlotPersistenceMapper mapper;

    public SlotRepositoryAdapter(SlotR2dbcRepository r2dbcRepository, SlotPersistenceMapper mapper) {
        this.r2dbcRepository = r2dbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Slot> findById(SlotId id) {
        return r2dbcRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Slot> findByDeliveryModeAndDate(DeliveryMode mode, LocalDate date) {
        return r2dbcRepository.findByDeliveryModeAndDate(mode.name(), date)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Slot> findAvailableByDeliveryModeAndDate(DeliveryMode mode, LocalDate date) {
        return r2dbcRepository.findAvailableByDeliveryModeAndDate(mode.name(), date)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Slot> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return r2dbcRepository.findByDateBetween(startDate, endDate)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Slot> findAll() {
        return r2dbcRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Slot> save(Slot slot) {
        return r2dbcRepository.save(mapper.toEntity(slot))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(SlotId id) {
        return r2dbcRepository.deleteById(id.value());
    }
}
