package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.token.RefreshToken;
import com.crafteam.delivery.domain.model.token.RefreshTokenId;
import com.crafteam.delivery.domain.model.token.TokenValue;
import com.crafteam.delivery.domain.model.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository port for RefreshToken aggregate.
 */
public interface RefreshTokenRepository {

    Mono<RefreshToken> save(RefreshToken refreshToken);

    Mono<RefreshToken> findById(RefreshTokenId id);

    Mono<RefreshToken> findByToken(TokenValue token);

    Flux<RefreshToken> findByUserId(UserId userId);

    Mono<Void> delete(RefreshTokenId id);

    Mono<Void> deleteByUserId(UserId userId);

    Mono<Void> deleteExpiredTokens();
}
