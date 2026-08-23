package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.adapter.JpaAuthSessionRepositoryAdapter;
import com.spsk1313.identityservice.identity.infrastructure.persistence.adapter.JpaRefreshTokenRepositoryAdapter;
import com.spsk1313.identityservice.identity.infrastructure.persistence.adapter.JpaUserRepositoryAdapter;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.AuthSessionPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
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
        AuthSessionPersistenceMapper.class,

        JpaRefreshTokenRepositoryAdapter.class,
        RefreshTokenPersistenceMapper.class
})
class JpaRefreshTokenRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private JpaUserRepositoryAdapter userRepository;

    @Autowired
    private JpaAuthSessionRepositoryAdapter authSessionRepository;

    @Autowired
    private JpaRefreshTokenRepositoryAdapter refreshTokenRepository;

    @Autowired
    private SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    private static final String TOKEN_HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final Instant NOW =
            Instant.parse("2026-08-22T12:00:00Z");

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-09-21T12:00:00Z");

    @Test
    void shouldSaveAndFindRefreshTokenByHash() {
        User user = createUser(
                "refresh@example.com"
        );

        AuthSession session = createSession(user);

        RefreshToken token = RefreshToken.issue(
                session.getId(),
                TOKEN_HASH,
                EXPIRES_AT
        );

        RefreshToken persisted =
                refreshTokenRepository.save(token);

        assertNotNull(persisted.getId());

        RefreshToken found = refreshTokenRepository
                .findByTokenHash(TOKEN_HASH)
                .orElseThrow();

        assertEquals(persisted.getId(), found.getId());
        assertEquals(session.getId(), found.getSessionId());
        assertEquals(TOKEN_HASH, found.getTokenHash());
        assertEquals(EXPIRES_AT, found.getExpiresAt());
        assertNull(found.getUsedAt());
    }

    @Test
    void shouldUpdateUsedAtWithoutLosingCreatedAt() {
        User user = createUser(
                "refresh-update@example.com"
        );

        AuthSession session = createSession(user);

        RefreshToken token =
                refreshTokenRepository.save(
                        RefreshToken.issue(
                                session.getId(),
                                TOKEN_HASH,
                                EXPIRES_AT
                        )
                );

        RefreshTokenJpaEntity beforeUpdate =
                springDataRefreshTokenRepository
                        .findById(token.getId())
                        .orElseThrow();

        Instant originalCreatedAt =
                beforeUpdate.getCreatedAt();

        token.use(NOW);

        refreshTokenRepository.save(token);

        RefreshTokenJpaEntity afterUpdate =
                springDataRefreshTokenRepository
                        .findById(token.getId())
                        .orElseThrow();

        assertEquals(
                NOW,
                afterUpdate.getUsedAt()
        );

        assertNotNull(afterUpdate.getCreatedAt());

        assertEquals(
                originalCreatedAt,
                afterUpdate.getCreatedAt()
        );
    }

    private User createUser(String email) {
        return userRepository.save(
                User.register(
                        new EmailAddress(email),
                        PASSWORD_HASH
                )
        );
    }

    private AuthSession createSession(User user) {
        return authSessionRepository.save(
                AuthSession.start(
                        user.getId(),
                        EXPIRES_AT,
                        "Mozilla/5.0"
                )
        );
    }
}