package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.EmailVerificationTokenPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaEmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    private final SpringDataEmailVerificationTokenRepository tokenRepository;
    private final EmailVerificationTokenPersistenceMapper tokenMapper;
    private final EntityManager entityManager;

    public JpaEmailVerificationTokenRepositoryAdapter(
            SpringDataEmailVerificationTokenRepository tokenRepository,
            EmailVerificationTokenPersistenceMapper tokenMapper,
            EntityManager entityManager
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenMapper = tokenMapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EmailVerificationToken> findOutstandingByUserId(Long userId) {
        Optional<EmailVerificationTokenJpaEntity> entity = tokenRepository
                .findByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNull(userId);

        return entity.map(tokenMapper::toDomain);
    }

    @Override
    public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {
        Optional<EmailVerificationTokenJpaEntity> entity = tokenRepository
                .findByTokenHash(tokenHash);

        return entity.map(tokenMapper::toDomain);
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {

        if (token.getId() == null) {
            UserJpaEntity userReference = entityManager.getReference(
                    UserJpaEntity.class,
                    token.getUserId()
            );

            EmailVerificationTokenJpaEntity entity =
                    tokenMapper.toEntity(token, userReference);

            entity = tokenRepository.save(entity);

            return tokenMapper.toDomain(entity);
        }

        EmailVerificationTokenJpaEntity existingEntity = tokenRepository
                .findById(token.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Verification token with id %d does not exist"
                                        .formatted(token.getId())
                        )
                );

        existingEntity.updateLifecycle(
                token.getUsedAt(),
                token.getInvalidatedAt()
        );

        existingEntity = tokenRepository.save(existingEntity);

        return tokenMapper.toDomain(existingEntity);
    }
}
