package com.crafteam.delivery.infrastructure.adapter.out.persistence.adapter;

import com.crafteam.delivery.application.port.out.BookingRepository;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.BookingId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.BookingPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.BookingR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing BookingRepository port using R2DBC.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingR2dbcRepository r2dbcRepository;
    private final BookingPersistenceMapper mapper;

    @Override
    public Mono<Booking> findById(BookingId id) {
        log.debug("Finding booking by id: {}", id.value());
        return r2dbcRepository.findById(id.value())
                .map(mapper::toDomain)
                .doOnSuccess(booking -> {
                    if (booking != null) {
                        log.debug("Found booking: {} with status: {}", id.value(), booking.getStatus());
                    } else {
                        log.debug("Booking not found with id: {}", id.value());
                    }
                })
                .doOnError(error -> log.error("Error finding booking by id: {}", id.value(), error));
    }

    @Override
    public Flux<Booking> findByUserId(UserId userId) {
        log.debug("Finding bookings by userId: {}", userId.value());
        return r2dbcRepository.findByUserId(userId.value())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding bookings for userId: {}", userId.value()))
                .doOnError(error -> log.error("Error finding bookings by userId: {}", userId.value(), error));
    }

    @Override
    public Flux<Booking> findBySlotId(SlotId slotId) {
        log.debug("Finding bookings by slotId: {}", slotId.value());
        return r2dbcRepository.findBySlotId(slotId.value())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding bookings for slotId: {}", slotId.value()))
                .doOnError(error -> log.error("Error finding bookings by slotId: {}", slotId.value(), error));
    }

    @Override
    public Mono<Booking> save(Booking booking) {
        log.info("Saving booking: {} for user: {}", booking.getId().value(), booking.getUserId().value());
        return r2dbcRepository.existsById(booking.getId().value())
                .flatMap(exists -> {
                    var entity = mapper.toEntity(booking);
                    if (exists) {
                        entity.markAsPersisted();
                    }
                    return r2dbcRepository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.info("Successfully saved booking: {} with status: {}",
                        saved.getId().value(), saved.getStatus()))
                .doOnError(error -> log.error("Error saving booking: {}", booking.getId().value(), error));
    }

    @Override
    public Mono<Void> delete(BookingId id) {
        log.info("Deleting booking: {}", id.value());
        return r2dbcRepository.deleteById(id.value())
                .doOnSuccess(v -> log.info("Successfully deleted booking: {}", id.value()))
                .doOnError(error -> log.error("Error deleting booking: {}", id.value(), error));
    }
}
