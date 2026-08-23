package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.AuthSessionPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaAuthSessionRepositoryAdapter implements AuthSessionRepository {

    private final SpringDataAuthSessionRepository sessionRepository;
    private final SpringDataUserRepository userRepository;
    private final AuthSessionPersistenceMapper mapper;

    public JpaAuthSessionRepositoryAdapter(
            SpringDataAuthSessionRepository sessionRepository,
            SpringDataUserRepository userRepository,
            AuthSessionPersistenceMapper mapper
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AuthSession save(AuthSession session) {

        if (session.getId() == null) {
            UserJpaEntity user = userRepository
                    .findById(session.getUserId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "User not found while persisting authentication session"
                            )
                    );

            AuthSessionJpaEntity entity =
                    mapper.toEntity(session, user);

            AuthSessionJpaEntity saved =
                    sessionRepository.save(entity);

            return mapper.toDomain(saved);
        }

        AuthSessionJpaEntity existing = sessionRepository
                .findById(session.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authentication session not found while updating"
                        )
                );

        mapper.updateEntity(session, existing);

        AuthSessionJpaEntity saved =
                sessionRepository.save(existing);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthSession> findById(Long id) {
        return sessionRepository
                .findById(id)
                .map(mapper::toDomain);
    }
}