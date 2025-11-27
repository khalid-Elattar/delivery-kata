package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.RefreshTokenCommand;
import com.crafteam.delivery.application.dto.response.TokenResponse;
import reactor.core.publisher.Mono;

/**
 * Use case for refreshing access tokens.
 */
public interface RefreshAccessTokenUseCase {
    Mono<TokenResponse> execute(RefreshTokenCommand command);
}
