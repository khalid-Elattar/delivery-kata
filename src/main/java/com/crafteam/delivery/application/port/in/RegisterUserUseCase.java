package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.RegisterUserCommand;
import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Mono;

/**
 * Input port for user registration.
 */
public interface RegisterUserUseCase {
    Mono<User> execute(RegisterUserCommand command);
}
