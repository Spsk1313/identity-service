package com.spsk1313.identityservice.identity.infrastructure.persistence.mapper;

import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationTokenPersistenceMapper {

    public EmailVerificationToken toDomain(EmailVerificationTokenJpaEntity entity) {
        return EmailVerificationToken.reconstitute(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getInvalidatedAt()
        );
    }

    public EmailVerificationTokenJpaEntity toEntity(EmailVerificationToken token, UserJpaEntity user) {
        return new EmailVerificationTokenJpaEntity(
                token.getId(),
                user,
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getInvalidatedAt()
        );
    }

}
