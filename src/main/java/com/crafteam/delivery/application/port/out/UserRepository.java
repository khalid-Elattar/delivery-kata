package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.User;
import com.crafteam.delivery.domain.model.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port for user persistence operations.
 */
public interface UserRepository {

    Mono<User> findById(UserId id);

    /**
     * Finds a user by ID and loads their bookings.
     */
    Mono<User> findByIdWithBookings(UserId id);

    Mono<User> findByEmail(Email email);

    /**
     * Finds a user by email and loads their bookings.
     */
    Mono<User> findByEmailWithBookings(Email email);

    Mono<Boolean> existsByEmail(Email email);

    Flux<User> findAll();

    /**
     * Finds all users and loads their bookings.
     */
    Flux<User> findAllWithBookings();

    Mono<User> save(User user);

    Mono<Void> delete(UserId id);
}
