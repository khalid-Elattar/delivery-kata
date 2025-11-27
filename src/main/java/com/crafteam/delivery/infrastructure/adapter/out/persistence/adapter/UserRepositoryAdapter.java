package com.crafteam.delivery.infrastructure.adapter.out.persistence.adapter;

import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.User;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.BookingPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.BookingR2dbcRepository;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Adapter implementing UserRepository port using R2DBC.
 * Follows R2DBC best practices: relationships are loaded separately, not eagerly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserR2dbcRepository userRepository;
    private final BookingR2dbcRepository bookingRepository;
    private final UserPersistenceMapper userMapper;
    private final BookingPersistenceMapper bookingMapper;

    @Override
    public Mono<User> findById(UserId id) {
        log.debug("Finding user by id: {}", id.value());
        return userRepository.findById(id.value())
                .map(userMapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user: {}", user.getEmail().value());
                    } else {
                        log.debug("User not found with id: {}", id.value());
                    }
                })
                .doOnError(error -> log.error("Error finding user by id: {}", id.value(), error));
    }

    @Override
    public Mono<User> findByIdWithBookings(UserId id) {
        log.debug("Finding user by id with bookings: {}", id.value());
        return userRepository.findById(id.value())
                .flatMap(userEntity -> {
                    User user = userMapper.toDomain(userEntity);
                    return loadBookingsForUser(user);
                })
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user with {} bookings: {}", user.getBookingsCount(), user.getEmail().value());
                    }
                })
                .doOnError(error -> log.error("Error finding user by id with bookings: {}", id.value(), error));
    }

    @Override
    public Mono<User> findByEmail(Email email) {
        log.debug("Finding user by email: {}", email.value());
        return userRepository.findByEmail(email.value())
                .map(userMapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user by email: {}", email.value());
                    } else {
                        log.debug("User not found with email: {}", email.value());
                    }
                })
                .doOnError(error -> log.error("Error finding user by email: {}", email.value(), error));
    }

    @Override
    public Mono<User> findByEmailWithBookings(Email email) {
        log.debug("Finding user by email with bookings: {}", email.value());
        return userRepository.findByEmail(email.value())
                .flatMap(userEntity -> {
                    User user = userMapper.toDomain(userEntity);
                    return loadBookingsForUser(user);
                })
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Found user with {} bookings by email: {}", user.getBookingsCount(), email.value());
                    }
                })
                .doOnError(error -> log.error("Error finding user by email with bookings: {}", email.value(), error));
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking if user exists by email: {}", email.value());
        return userRepository.existsByEmail(email.value())
                .doOnSuccess(exists -> log.debug("User exists by email {}: {}", email.value(), exists))
                .doOnError(error -> log.error("Error checking user existence by email: {}", email.value(), error));
    }

    @Override
    public Flux<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll()
                .map(userMapper::toDomain)
                .doOnComplete(() -> log.debug("Completed finding all users"))
                .doOnError(error -> log.error("Error finding all users", error));
    }

    @Override
    public Flux<User> findAllWithBookings() {
        log.debug("Finding all users with bookings");
        return userRepository.findAll()
                .flatMap(userEntity -> {
                    User user = userMapper.toDomain(userEntity);
                    return loadBookingsForUser(user);
                })
                .doOnComplete(() -> log.debug("Completed finding all users with bookings"))
                .doOnError(error -> log.error("Error finding all users with bookings", error));
    }

    @Override
    public Mono<User> save(User user) {
        log.info("Saving user: {}", user.getEmail().value());
        return userRepository.save(userMapper.toEntity(user))
                .map(userMapper::toDomain)
                .doOnSuccess(savedUser -> log.info("Successfully saved user: {}", savedUser.getEmail().value()))
                .doOnError(error -> log.error("Error saving user: {}", user.getEmail().value(), error));
    }

    @Override
    public Mono<Void> delete(UserId id) {
        log.info("Deleting user: {}", id.value());
        return userRepository.deleteById(id.value())
                .doOnSuccess(v -> log.info("Successfully deleted user: {}", id.value()))
                .doOnError(error -> log.error("Error deleting user: {}", id.value(), error));
    }

    /**
     * Loads bookings for a user reactively.
     * This follows R2DBC best practices where relationships are loaded separately.
     */
    private Mono<User> loadBookingsForUser(User user) {
        return bookingRepository.findByUserId(user.getId().value())
                .map(bookingMapper::toDomain)
                .collectList()
                .map(bookings -> {
                    user.addBookings(bookings);
                    return user;
                })
                .doOnSuccess(u -> log.trace("Loaded {} bookings for user: {}", u.getBookingsCount(), u.getId().value()));
    }
}
