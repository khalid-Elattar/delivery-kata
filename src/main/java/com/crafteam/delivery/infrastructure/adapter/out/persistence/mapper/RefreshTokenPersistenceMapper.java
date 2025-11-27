package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.token.RefreshToken;
import com.crafteam.delivery.domain.model.token.RefreshTokenId;
import com.crafteam.delivery.domain.model.token.TokenValue;
import com.crafteam.delivery.domain.model.user.UserId;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;

import java.util.UUID;

/**
 * MapStruct mapper for RefreshToken domain ↔ entity conversions.
 */
@Mapper(componentModel = "spring")
public interface RefreshTokenPersistenceMapper {

    default RefreshTokenEntity toEntity(RefreshToken refreshToken) {
        return RefreshTokenEntity.builder()
                .id(UUID.fromString(refreshToken.getId().toString()))
                .token(refreshToken.getToken().value())
                .userId(UUID.fromString(refreshToken.getUserId().toString()))
                .issuedAt(refreshToken.getIssuedAt())
                .expiresAt(refreshToken.getExpiresAt())
                .revoked(refreshToken.isRevoked())
                .revokedAt(refreshToken.getRevokedAt())
                .isNew(true)
                .build();
    }

    default RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.reconstitute(
                RefreshTokenId.from(entity.getId().toString()),
                TokenValue.from(entity.getToken()),
                UserId.from(entity.getUserId().toString()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getRevoked(),
                entity.getRevokedAt()
        );
    }
}
