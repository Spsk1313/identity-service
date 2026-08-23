package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RevokeAuthSessionServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    private RevokeAuthSessionService service;

    private static final Instant NOW =
            Instant.parse("2026-08-23T12:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new RevokeAuthSessionService(
                authSessionRepository,
                clock
        );
    }

    @Test
    void shouldRevokeAndPersistAuthSession() {
        AuthSession session = AuthSession.reconstitute(
                10L,
                1L,
                NOW.plusSeconds(3600),
                null,
                null,
                "Test Browser"
        );

        service.revoke(session);

        assertTrue(session.isRevoked());
        assertEquals(NOW, session.getRevokedAt());

        verify(authSessionRepository).save(session);
    }
}