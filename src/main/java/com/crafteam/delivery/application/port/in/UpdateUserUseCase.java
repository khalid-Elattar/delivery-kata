package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.ChangePasswordCommand;
import com.crafteam.delivery.application.dto.command.UpdateUserCommand;
import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Mono;

/**
 * Input port for updating user information.
 */
public interface UpdateUserUseCase {
    Mono<User> execute(UpdateUserCommand command);

    Mono<User> changePassword(ChangePasswordCommand command);
}
