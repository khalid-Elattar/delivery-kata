package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.LogoutCommand;
import reactor.core.publisher.Mono;

/**
 * Use case for user logout (revoke refresh tokens).
 */
public interface LogoutUserUseCase {
    Mono<Void> execute(LogoutCommand command);
}
