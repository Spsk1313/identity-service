package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.application.port.out.PasswordResetTokenRepository;
import com.spsk1313.identityservice.identity.domain.auth.PasswordResetToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.PasswordResetTokenPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class JpaPasswordResetTokenRepositoryAdapter
        implements PasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository repository;
    private final PasswordResetTokenPersistenceMapper mapper;

    public JpaPasswordResetTokenRepositoryAdapter(
            SpringDataPasswordResetTokenRepository repository,
            PasswordResetTokenPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PasswordResetToken save(
            PasswordResetToken token
    ) {
        PasswordResetTokenJpaEntity entity =
                mapper.toEntity(token);

        PasswordResetTokenJpaEntity saved =
                repository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    ) {
        return repository
                .findByTokenHash(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    public void invalidateAllUnusedByUserId(Long userId, Instant usedAt) {
        repository.invalidateAllUnusedByUserId(userId, usedAt);
    }
}