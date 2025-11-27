package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.LoginCommand;
import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Mono;

/**
 * Input port for user login.
 */
public interface LoginUserUseCase {
    Mono<User> execute(LoginCommand command);
}
