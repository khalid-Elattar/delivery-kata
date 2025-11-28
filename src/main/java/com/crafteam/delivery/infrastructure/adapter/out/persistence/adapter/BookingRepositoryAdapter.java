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
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Adapter implementing BookingRepository port using R2DBC.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingR2dbcRepository r2dbcRepository;
    private final BookingPersistenceMapper mapper;
    private final DatabaseClient databaseClient;

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

    @Override
    public Mono<Long> countBySlotIdAndDateAndTime(SlotId slotId, LocalDate date, LocalTime time) {
        log.debug("Counting bookings for slotId={}, date={}, time={}", slotId.value(), date, time);
        return databaseClient.sql("""
                SELECT COUNT(*) FROM bookings
                WHERE slot_id = :slotId
                AND booking_date = :date
                AND booking_time = :time
                AND status != 'CANCELLED'
                """)
                .bind("slotId", slotId.value())
                .bind("date", date)
                .bind("time", time)
                .map(row -> row.get(0, Long.class))
                .one()
                .defaultIfEmpty(0L)
                .doOnSuccess(count -> log.debug("Found {} bookings for slot/date/time", count))
                .doOnError(error -> log.error("Error counting bookings", error));
    }

    @Override
    public Mono<Boolean> existsBySlotIdAndUserIdAndDateAndTime(
            SlotId slotId, UserId userId, LocalDate date, LocalTime time) {
        log.debug("Checking if booking exists for user={}, slot={}, date={}, time={}",
                userId.value(), slotId.value(), date, time);
        return databaseClient.sql("""
                SELECT COUNT(*) > 0 FROM bookings
                WHERE slot_id = :slotId
                AND user_id = :userId
                AND booking_date = :date
                AND booking_time = :time
                AND status != 'CANCELLED'
                """)
                .bind("slotId", slotId.value())
                .bind("userId", userId.value())
                .bind("date", date)
                .bind("time", time)
                .map(row -> row.get(0, Boolean.class))
                .one()
                .defaultIfEmpty(false)
                .doOnSuccess(exists -> log.debug("User booking exists: {}", exists))
                .doOnError(error -> log.error("Error checking user booking existence", error));
    }

    @Override
    public Mono<Long> countActiveByUserId(UserId userId) {
        log.debug("Counting active bookings for userId={}", userId.value());
        return databaseClient.sql("""
                SELECT COUNT(*) FROM bookings
                WHERE user_id = :userId
                AND status != 'CANCELLED'
                """)
                .bind("userId", userId.value())
                .map(row -> row.get(0, Long.class))
                .one()
                .defaultIfEmpty(0L)
                .doOnSuccess(count -> log.debug("User has {} active bookings", count))
                .doOnError(error -> log.error("Error counting active bookings", error));
    }

    @Override
    public Flux<LocalTime> findBookedTimesForDate(SlotId slotId, LocalDate date) {
        log.debug("Finding booked times for slotId={}, date={}", slotId.value(), date);
        return databaseClient.sql("""
                SELECT booking_time FROM bookings
                WHERE slot_id = :slotId
                AND booking_date = :date
                AND status != 'CANCELLED'
                """)
                .bind("slotId", slotId.value())
                .bind("date", date)
                .map(row -> row.get("booking_time", LocalTime.class))
                .all()
                .doOnComplete(() -> log.debug("Completed finding booked times"))
                .doOnError(error -> log.error("Error finding booked times", error));
    }
}
