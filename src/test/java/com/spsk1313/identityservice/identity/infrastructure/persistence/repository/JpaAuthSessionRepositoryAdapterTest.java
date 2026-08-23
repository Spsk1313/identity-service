package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.AuthSessionPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import({
        JpaUserRepositoryAdapter.class,
        UserPersistenceMapper.class,
        JpaAuthSessionRepositoryAdapter.class,
        AuthSessionPersistenceMapper.class
})
class JpaAuthSessionRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private JpaUserRepositoryAdapter userRepository;

    @Autowired
    private JpaAuthSessionRepositoryAdapter authSessionRepository;

    @Autowired
    private SpringDataAuthSessionRepository springDataAuthSessionRepository;

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-09-21T12:00:00Z");

    @Test
    void shouldSaveAndFindAuthSession() {
        User user = userRepository.save(
                User.register(
                        new EmailAddress("session@example.com"),
                        PASSWORD_HASH
                )
        );

        AuthSession session = AuthSession.start(
                user.getId(),
                EXPIRES_AT,
                "Mozilla/5.0"
        );

        AuthSession persisted =
                authSessionRepository.save(session);

        assertNotNull(persisted.getId());

        AuthSession found = authSessionRepository
                .findById(persisted.getId())
                .orElseThrow();

        assertEquals(user.getId(), found.getUserId());
        assertEquals(EXPIRES_AT, found.getExpiresAt());
        assertEquals("Mozilla/5.0", found.getUserAgent());
        assertNull(found.getRevokedAt());
        assertNull(found.getLastUsedAt());
    }

    @Test
    void shouldUpdateExistingSessionWithoutLosingCreatedAt() {
        User user = userRepository.save(
                User.register(
                        new EmailAddress("session-update@example.com"),
                        PASSWORD_HASH
                )
        );

        AuthSession session = authSessionRepository.save(
                AuthSession.start(
                        user.getId(),
                        EXPIRES_AT,
                        "Mozilla/5.0"
                )
        );

        AuthSessionJpaEntity beforeUpdate =
                springDataAuthSessionRepository
                        .findById(session.getId())
                        .orElseThrow();

        Instant originalCreatedAt =
                beforeUpdate.getCreatedAt();

        Instant usedAt =
                Instant.parse("2026-08-22T13:00:00Z");

        session.markUsed(usedAt);
        session.revoke(usedAt.plusSeconds(60));

        authSessionRepository.save(session);

        AuthSessionJpaEntity afterUpdate =
                springDataAuthSessionRepository
                        .findById(session.getId())
                        .orElseThrow();

        assertEquals(
                usedAt,
                afterUpdate.getLastUsedAt()
        );

        assertEquals(
                usedAt.plusSeconds(60),
                afterUpdate.getRevokedAt()
        );

        assertNotNull(afterUpdate.getCreatedAt());

        assertEquals(
                originalCreatedAt,
                afterUpdate.getCreatedAt()
        );
    }
}