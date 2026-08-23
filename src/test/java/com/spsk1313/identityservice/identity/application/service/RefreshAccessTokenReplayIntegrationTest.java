package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.RefreshAccessTokenCommand;
import com.spsk1313.identityservice.identity.application.exception.InvalidRefreshTokenException;
import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.application.port.out.RefreshTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=1025",
        "app.verification.base-url=http://localhost:8080",
        "app.jwt.issuer=http://localhost:8080",
        "app.jwt.access-token-ttl=15m",
        "app.jwt.private-key=file:keys/private.pem",
        "app.jwt.public-key=file:keys/public.pem"
})
@Testcontainers
class RefreshAccessTokenReplayIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private RefreshAccessTokenService refreshAccessTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private com.spsk1313.identityservice.identity.application.port.out.TokenHasher tokenHasher;

    private static final String RAW_REFRESH_TOKEN =
            "replay-test-refresh-token";

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    @Test
    void shouldPersistSessionRevocationWhenUsedRefreshTokenIsReplayed() {
        User user = userRepository.save(
                User.register(
                        new EmailAddress("replay-test@example.com"),
                        PASSWORD_HASH
                )
        );

        Instant expiresAt =
                Instant.now().plusSeconds(3600);

        AuthSession session = authSessionRepository.save(
                AuthSession.start(
                        user.getId(),
                        expiresAt,
                        "Integration Test"
                )
        );

        String tokenHash =
                tokenHasher.hash(RAW_REFRESH_TOKEN);

        RefreshToken token = RefreshToken.issue(
                session.getId(),
                tokenHash,
                expiresAt
        );

        token = refreshTokenRepository.save(token);

        token.use(Instant.now());
        refreshTokenRepository.save(token);

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshAccessTokenService.refresh(
                        new RefreshAccessTokenCommand(
                                RAW_REFRESH_TOKEN
                        )
                )
        );

        AuthSession reloadedSession =
                authSessionRepository
                        .findById(session.getId())
                        .orElseThrow();

        assertTrue(reloadedSession.isRevoked());
        assertNotNull(reloadedSession.getRevokedAt());
    }
}