package com.crafteam.delivery.infrastructure.adapter.out.persistence.adapter;

import com.crafteam.delivery.application.port.out.RefreshTokenRepository;
import com.crafteam.delivery.domain.model.token.RefreshToken;
import com.crafteam.delivery.domain.model.token.RefreshTokenId;
import com.crafteam.delivery.domain.model.token.TokenValue;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper.RefreshTokenPersistenceMapper;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.repository.RefreshTokenR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Adapter implementing RefreshTokenRepository port using R2DBC.
 */
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenR2dbcRepository r2dbcRepository;
    private final RefreshTokenPersistenceMapper mapper;

    public RefreshTokenRepositoryAdapter(RefreshTokenR2dbcRepository r2dbcRepository,
                                        RefreshTokenPersistenceMapper mapper) {
        this.r2dbcRepository = r2dbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<RefreshToken> save(RefreshToken refreshToken) {
        return Mono.just(refreshToken)
                .map(mapper::toEntity)
                .flatMap(r2dbcRepository::save)
                .map(entity -> entity.markAsPersisted())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<RefreshToken> findById(RefreshTokenId id) {
        return r2dbcRepository.findById(UUID.fromString(id.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<RefreshToken> findByToken(TokenValue token) {
        return r2dbcRepository.findByToken(token.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<RefreshToken> findByUserId(UserId userId) {
        return r2dbcRepository.findByUserId(UUID.fromString(userId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(RefreshTokenId id) {
        return r2dbcRepository.deleteById(UUID.fromString(id.toString()));
    }

    @Override
    public Mono<Void> deleteByUserId(UserId userId) {
        return r2dbcRepository.deleteByUserId(UUID.fromString(userId.toString()));
    }

    @Override
    public Mono<Void> deleteExpiredTokens() {
        return r2dbcRepository.deleteExpiredTokens(Instant.now());
    }
}
