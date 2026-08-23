package com.spsk1313.identityservice.identity.infrastructure.persistence.mapper;

import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionPersistenceMapper {

    public AuthSession toDomain(AuthSessionJpaEntity entity) {
        return AuthSession.reconstitute(
                entity.getId(),
                entity.getUser().getId(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getLastUsedAt(),
                entity.getUserAgent()
        );
    }

    public AuthSessionJpaEntity toEntity(
            AuthSession session,
            UserJpaEntity user
    ) {
        return new AuthSessionJpaEntity(
                session.getId(),
                user,
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getLastUsedAt(),
                session.getUserAgent()
        );
    }

    public void updateEntity(
            AuthSession session,
            AuthSessionJpaEntity entity
    ) {
        entity.updateFrom(
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getLastUsedAt(),
                session.getUserAgent()
        );
    }
}