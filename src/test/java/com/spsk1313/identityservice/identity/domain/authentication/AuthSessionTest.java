package com.spsk1313.identityservice.identity.domain.authentication;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthSessionTest {

    private static final Long USER_ID = 1L;

    private static final Instant NOW =
            Instant.parse("2026-08-22T12:00:00Z");

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-09-21T12:00:00Z");

    private static final String USER_AGENT =
            "Mozilla/5.0";

    @Test
    void shouldStartActiveSession() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        assertNull(session.getId());
        assertEquals(USER_ID, session.getUserId());
        assertEquals(EXPIRES_AT, session.getExpiresAt());
        assertEquals(USER_AGENT, session.getUserAgent());

        assertNull(session.getRevokedAt());
        assertNull(session.getLastUsedAt());

        assertFalse(session.isRevoked());
        assertFalse(session.isExpired(NOW));
        assertTrue(session.isActive(NOW));
    }

    @Test
    void shouldDetermineSessionIsExpiredBeforeAtAndAfterExpiration() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        Instant beforeExpiration =
                EXPIRES_AT.minusSeconds(1);

        Instant afterExpiration =
                EXPIRES_AT.plusSeconds(1);

        assertFalse(session.isExpired(beforeExpiration));

        assertTrue(session.isExpired(EXPIRES_AT));

        assertTrue(session.isExpired(afterExpiration));
    }

    @Test
    void shouldRevokeSession() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        session.revoke(NOW);

        assertTrue(session.isRevoked());
        assertEquals(NOW, session.getRevokedAt());
        assertFalse(session.isActive(NOW));
    }

    @Test
    void shouldPreserveOriginalRevocationTimeWhenRevokedAgain() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        Instant originalRevocationTime = NOW;
        Instant secondRevocationTime = NOW.plusSeconds(60);

        session.revoke(originalRevocationTime);
        session.revoke(secondRevocationTime);

        assertEquals(
                originalRevocationTime,
                session.getRevokedAt()
        );
    }

    @Test
    void shouldMarkSessionAsUsed() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        session.markUsed(NOW);

        assertEquals(
                NOW,
                session.getLastUsedAt()
        );
    }

    @Test
    void shouldReconstituteExistingSession() {
        Instant revokedAt =
                Instant.parse("2026-08-21T12:00:00Z");

        Instant lastUsedAt =
                Instant.parse("2026-08-20T12:00:00Z");

        AuthSession session = AuthSession.reconstitute(
                10L,
                USER_ID,
                EXPIRES_AT,
                revokedAt,
                lastUsedAt,
                USER_AGENT
        );

        assertEquals(10L, session.getId());
        assertEquals(USER_ID, session.getUserId());
        assertEquals(EXPIRES_AT, session.getExpiresAt());
        assertEquals(revokedAt, session.getRevokedAt());
        assertEquals(lastUsedAt, session.getLastUsedAt());
        assertEquals(USER_AGENT, session.getUserAgent());

        assertTrue(session.isRevoked());
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthSession.start(
                        null,
                        EXPIRES_AT,
                        USER_AGENT
                )
        );
    }

    @Test
    void shouldRejectNullExpiration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthSession.start(
                        USER_ID,
                        null,
                        USER_AGENT
                )
        );
    }

    @Test
    void shouldRejectNullCurrentTimeWhenCheckingExpiration() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session.isExpired(null)
        );
    }

    @Test
    void shouldRejectNullRevocationTime() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session.revoke(null)
        );
    }

    @Test
    void shouldRejectNullLastUsedTime() {
        AuthSession session = AuthSession.start(
                USER_ID,
                EXPIRES_AT,
                USER_AGENT
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session.markUsed(null)
        );
    }
}