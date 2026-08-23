package com.spsk1313.identityservice.identity.domain.auth;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    private static final Long SESSION_ID = 10L;

    private static final String TOKEN_HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final Instant NOW =
            Instant.parse("2026-08-22T12:00:00Z");

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-09-21T12:00:00Z");

    @Test
    void shouldIssueUnusedToken() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        assertNull(token.getId());
        assertEquals(SESSION_ID, token.getSessionId());
        assertEquals(TOKEN_HASH, token.getTokenHash());
        assertEquals(EXPIRES_AT, token.getExpiresAt());
        assertNull(token.getUsedAt());

        assertFalse(token.isUsed());
        assertFalse(token.isExpired(NOW));
        assertTrue(token.isUsable(NOW));
    }

    @Test
    void shouldConsiderTokenExpiredAtExpirationInstant() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        Instant beforeExpiration = EXPIRES_AT.minusSeconds(1);
        Instant afterExpiration = EXPIRES_AT.plusSeconds(1);

        assertFalse(token.isExpired(beforeExpiration));

        assertTrue(token.isExpired(EXPIRES_AT));

        assertTrue(token.isExpired(afterExpiration));
    }

    @Test
    void shouldUseValidToken() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        assertFalse(token.isUsed());
        assertTrue(token.isUsable(NOW));

        token.use(NOW);

        assertTrue(token.isUsed());
        assertEquals(NOW, token.getUsedAt());
        assertFalse(token.isUsable(NOW));
    }

    @Test
    void shouldRejectExpiredToken() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        assertThrows(
                RefreshTokenExpiredException.class,
                () -> token.use(EXPIRES_AT)
        );

        assertFalse(token.isUsed());
        assertNull(token.getUsedAt());
    }

    @Test
    void shouldRejectAlreadyUsedToken() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        token.use(NOW);

        Instant originalUsedAt = token.getUsedAt();

        assertThrows(
                RefreshTokenAlreadyUsedException.class,
                () -> token.use(NOW.plusSeconds(60))
        );

        assertEquals(originalUsedAt, token.getUsedAt());
    }

    @Test
    void shouldReconstituteExistingToken() {
        Instant usedAt =
                Instant.parse("2026-08-22T11:00:00Z");

        RefreshToken token = RefreshToken.reconstitute(
                25L,
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT,
                usedAt
        );

        assertEquals(25L, token.getId());
        assertEquals(SESSION_ID, token.getSessionId());
        assertEquals(TOKEN_HASH, token.getTokenHash());
        assertEquals(EXPIRES_AT, token.getExpiresAt());
        assertEquals(usedAt, token.getUsedAt());

        assertTrue(token.isUsed());
        assertFalse(token.isUsable(NOW));
    }

    @Test
    void shouldRejectNullSessionId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.issue(
                        null,
                        TOKEN_HASH,
                        EXPIRES_AT
                )
        );
    }

    @Test
    void shouldRejectNullTokenHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.issue(
                        SESSION_ID,
                        null,
                        EXPIRES_AT
                )
        );
    }

    @Test
    void shouldRejectBlankTokenHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.issue(
                        SESSION_ID,
                        "   ",
                        EXPIRES_AT
                )
        );
    }

    @Test
    void shouldRejectNullExpiration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.issue(
                        SESSION_ID,
                        TOKEN_HASH,
                        null
                )
        );
    }

    @Test
    void shouldRejectNullCurrentTimeWhenCheckingExpiration() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> token.isExpired(null)
        );
    }

    @Test
    void shouldRejectNullUsageTime() {
        RefreshToken token = RefreshToken.issue(
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> token.use(null)
        );
    }
}