package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Mono;

/**
 * Input port for retrieving user information.
 */
public interface GetUserUseCase {
    Mono<User> findById(String userId);

    Mono<User> findByEmail(String email);
}
