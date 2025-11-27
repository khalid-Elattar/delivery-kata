package com.crafteam.delivery.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Input port for deleting users.
 */
public interface DeleteUserUseCase {
    Mono<Void> execute(String userId);
}
