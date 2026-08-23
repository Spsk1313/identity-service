package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.port.out.RefreshTokenRepository;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataAuthSessionRepository;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository refreshTokenRepository;
    private final SpringDataAuthSessionRepository authSessionRepository;
    private final RefreshTokenPersistenceMapper mapper;

    public JpaRefreshTokenRepositoryAdapter(
            SpringDataRefreshTokenRepository refreshTokenRepository,
            SpringDataAuthSessionRepository authSessionRepository,
            RefreshTokenPersistenceMapper mapper
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authSessionRepository = authSessionRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {

        if (token.getId() == null) {
            AuthSessionJpaEntity session = authSessionRepository
                    .findById(token.getSessionId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Authentication session not found while persisting refresh token"
                            )
                    );

            RefreshTokenJpaEntity entity =
                    mapper.toEntity(token, session);

            RefreshTokenJpaEntity saved =
                    refreshTokenRepository.save(entity);

            return mapper.toDomain(saved);
        }

        RefreshTokenJpaEntity existing = refreshTokenRepository
                .findById(token.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Refresh token not found while updating"
                        )
                );

        mapper.updateEntity(token, existing);

        RefreshTokenJpaEntity saved =
                refreshTokenRepository.save(existing);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .map(mapper::toDomain);
    }
}