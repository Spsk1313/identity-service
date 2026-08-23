package com.spsk1313.identityservice.identity.infrastructure.persistence.mapper;

import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {

    public PasswordResetToken toDomain(
            PasswordResetTokenJpaEntity entity
    ) {
        return PasswordResetToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt()
        );
    }

    public PasswordResetTokenJpaEntity toEntity(
            PasswordResetToken token
    ) {
        return new PasswordResetTokenJpaEntity(
                token.getId(),
                token.getUserId(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt()
        );
    }

}
