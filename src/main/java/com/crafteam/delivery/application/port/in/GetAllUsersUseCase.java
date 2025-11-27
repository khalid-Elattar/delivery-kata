package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Flux;

/**
 * Input port for retrieving all users (admin only).
 */
public interface GetAllUsersUseCase {
    Flux<User> execute();
}
