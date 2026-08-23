package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import com.spsk1313.identityservice.identity.infrastructure.persistence.adapter.JpaEmailVerificationTokenRepositoryAdapter;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.EmailVerificationTokenPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import({
        JpaEmailVerificationTokenRepositoryAdapter.class,
        EmailVerificationTokenPersistenceMapper.class
})
class JpaEmailVerificationTokenRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    private static final Instant NOW =
            Instant.parse("2026-08-18T12:00:00Z");

    private static final Instant EXPIRES_AT =
            NOW.plusSeconds(3600);

    private static final String TOKEN_HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Test
    void shouldSaveNewVerificationToken() {
        UserJpaEntity user = createPersistedUser();

        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        user.getId(),
                        TOKEN_HASH,
                        EXPIRES_AT
                );

        EmailVerificationToken savedToken =
                tokenRepository.save(token);

        assertNotNull(savedToken.getId());
        assertEquals(user.getId(), savedToken.getUserId());
        assertEquals(TOKEN_HASH, savedToken.getTokenHash());
        assertEquals(EXPIRES_AT, savedToken.getExpiresAt());
        assertFalse(savedToken.isUsed());
        assertFalse(savedToken.isInvalidated());
    }

    @Test
    void shouldFindVerificationTokenByTokenHash() {
        UserJpaEntity user = createPersistedUser();

        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        user.getId(),
                        TOKEN_HASH,
                        EXPIRES_AT
                );

        EmailVerificationToken savedToken =
                tokenRepository.save(token);

        Optional<EmailVerificationToken> retrievedToken =
                tokenRepository.findByTokenHash(TOKEN_HASH);

        assertTrue(retrievedToken.isPresent());

        EmailVerificationToken actual = retrievedToken.get();

        assertEquals(savedToken.getId(), actual.getId());
        assertEquals(savedToken.getUserId(), actual.getUserId());
        assertEquals(TOKEN_HASH, actual.getTokenHash());
        assertEquals(EXPIRES_AT, actual.getExpiresAt());
    }

    @Test
    void shouldFindOutstandingVerificationTokenByUserId() {
        UserJpaEntity user = createPersistedUser();

        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        user.getId(),
                        TOKEN_HASH,
                        EXPIRES_AT
                );

        EmailVerificationToken savedToken =
                tokenRepository.save(token);

        Optional<EmailVerificationToken> outstandingToken =
                tokenRepository.findOutstandingByUserId(user.getId());

        assertTrue(outstandingToken.isPresent());
        assertEquals(
                savedToken.getId(),
                outstandingToken.get().getId()
        );

        savedToken.invalidate(NOW);
        tokenRepository.save(savedToken);

        Optional<EmailVerificationToken> afterInvalidation =
                tokenRepository.findOutstandingByUserId(user.getId());

        assertTrue(afterInvalidation.isEmpty());
    }

    @Test
    void shouldUpdateExistingVerificationTokenWhenInvalidated() {
        UserJpaEntity user = createPersistedUser();

        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        user.getId(),
                        TOKEN_HASH,
                        EXPIRES_AT
                );

        EmailVerificationToken savedToken =
                tokenRepository.save(token);

        Long originalId = savedToken.getId();

        savedToken.invalidate(NOW);
        tokenRepository.save(savedToken);

        Optional<EmailVerificationToken> reloadedToken =
                tokenRepository.findByTokenHash(TOKEN_HASH);

        assertTrue(reloadedToken.isPresent());

        EmailVerificationToken actual = reloadedToken.get();

        assertEquals(originalId, actual.getId());
        assertEquals(NOW, actual.getInvalidatedAt());
        assertTrue(actual.isInvalidated());
    }

    private UserJpaEntity createPersistedUser() {
        UserJpaEntity user = new UserJpaEntity(
                null,
                "sahil@example.com",
                PASSWORD_HASH,
                false,
                AccountStatus.ACTIVE
        );

        return userRepository.save(user);
    }
}