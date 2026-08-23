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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutAllServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    private LogoutAllService logoutAllService;

    private static final Long USER_ID = 8L;

    private static final Instant NOW =
            Instant.parse("2026-08-23T18:00:00Z");

    private static final Instant EXPIRES_AT =
            NOW.plusSeconds(3600);

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        logoutAllService = new LogoutAllService(
                authSessionRepository,
                clock
        );
    }

    @Test
    void shouldRevokeAllActiveSessionsForUser() {
        AuthSession firstSession = activeSession(
                1L,
                "Chrome"
        );

        AuthSession secondSession = activeSession(
                2L,
                "iPhone"
        );

        AuthSession thirdSession = activeSession(
                3L,
                "Laptop"
        );

        when(authSessionRepository.findByUserId(USER_ID))
                .thenReturn(
                        List.of(
                                firstSession,
                                secondSession,
                                thirdSession
                        )
                );

        logoutAllService.logoutAll(USER_ID);

        assertTrue(firstSession.isRevoked());
        assertTrue(secondSession.isRevoked());
        assertTrue(thirdSession.isRevoked());

        assertEquals(NOW, firstSession.getRevokedAt());
        assertEquals(NOW, secondSession.getRevokedAt());
        assertEquals(NOW, thirdSession.getRevokedAt());

        verify(authSessionRepository)
                .save(firstSession);

        verify(authSessionRepository)
                .save(secondSession);

        verify(authSessionRepository)
                .save(thirdSession);
    }

    @Test
    void shouldOnlyRevokeActiveSessionsWhenSomeSessionsAreAlreadyRevoked() {
        AuthSession firstActiveSession =
                activeSession(1L, "Chrome");

        Instant originalRevokedAt =
                NOW.minusSeconds(600);

        AuthSession alreadyRevokedSession =
                revokedSession(
                        2L,
                        originalRevokedAt,
                        "iPhone"
                );

        AuthSession secondActiveSession =
                activeSession(3L, "Laptop");

        when(authSessionRepository.findByUserId(USER_ID))
                .thenReturn(
                        List.of(
                                firstActiveSession,
                                alreadyRevokedSession,
                                secondActiveSession
                        )
                );

        logoutAllService.logoutAll(USER_ID);

        assertEquals(
                NOW,
                firstActiveSession.getRevokedAt()
        );

        assertEquals(
                NOW,
                secondActiveSession.getRevokedAt()
        );

        assertEquals(
                originalRevokedAt,
                alreadyRevokedSession.getRevokedAt()
        );

        verify(authSessionRepository)
                .save(firstActiveSession);

        verify(authSessionRepository)
                .save(secondActiveSession);

        verify(authSessionRepository, never())
                .save(alreadyRevokedSession);
    }

    @Test
    void shouldDoNothingWhenUserHasNoSessions() {
        when(authSessionRepository.findByUserId(USER_ID))
                .thenReturn(List.of());

        logoutAllService.logoutAll(USER_ID);

        verify(authSessionRepository)
                .findByUserId(USER_ID);

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> logoutAllService.logoutAll(null)
        );

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void shouldDoNothingWhenAllSessionsAreAlreadyRevoked() {
        Instant firstRevokedAt =
                NOW.minusSeconds(1200);

        Instant secondRevokedAt =
                NOW.minusSeconds(600);

        AuthSession firstSession =
                revokedSession(
                        1L,
                        firstRevokedAt,
                        "Chrome"
                );

        AuthSession secondSession =
                revokedSession(
                        2L,
                        secondRevokedAt,
                        "iPhone"
                );

        when(authSessionRepository.findByUserId(USER_ID))
                .thenReturn(
                        List.of(
                                firstSession,
                                secondSession
                        )
                );

        logoutAllService.logoutAll(USER_ID);

        assertTrue(firstSession.isRevoked());
        assertTrue(secondSession.isRevoked());

        assertEquals(
                firstRevokedAt,
                firstSession.getRevokedAt()
        );

        assertEquals(
                secondRevokedAt,
                secondSession.getRevokedAt()
        );

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    private AuthSession activeSession(
            Long sessionId,
            String userAgent
    ) {
        return AuthSession.reconstitute(
                sessionId,
                USER_ID,
                EXPIRES_AT,
                null,
                null,
                userAgent
        );
    }

    private AuthSession revokedSession(
            Long sessionId,
            Instant revokedAt,
            String userAgent
    ) {
        return AuthSession.reconstitute(
                sessionId,
                USER_ID,
                EXPIRES_AT,
                revokedAt,
                null,
                userAgent
        );
    }
}