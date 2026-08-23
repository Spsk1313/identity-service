package com.spsk1313.identityservice.identity.infrastructure.persistence.mapper;

import com.spsk1313.identityservice.identity.domain.auth.RefreshToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getSession().getId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt()
        );
    }

    public RefreshTokenJpaEntity toEntity(
            RefreshToken token,
            AuthSessionJpaEntity session
    ) {
        return new RefreshTokenJpaEntity(
                token.getId(),
                session,
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt()
        );
    }

    public void updateEntity(
            RefreshToken token,
            RefreshTokenJpaEntity entity
    ) {
        entity.updateUsedAt(token.getUsedAt());
    }
}