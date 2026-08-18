package com.spsk1313.identityservice.identity.domain.verification;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class EmailVerificationTokenTest {

    private static final Instant NOW = Instant.parse("2026-08-18T12:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plusSeconds(3600);


    @Test
    void shouldIssueUnusedAndNonInvalidatedToken() {
        EmailVerificationToken token = createToken();
        assertNull(token.getUsedAt());
        assertNull(token.getInvalidatedAt());
    }

    @Test
    void shouldConsiderTokenExpiredAtExpirationInstant() {
        EmailVerificationToken token = createToken();
        assertTrue(token.isExpired(EXPIRES_AT));
    }

    @Test
    void shouldUseUsableToken() {
        EmailVerificationToken token = createToken();
        assertTrue(token.isUsable(NOW));
        token.use(NOW);
        assertTrue(token.isUsed());
    }

    @Test
    void shouldRejectUsingExpiredToken() {
        EmailVerificationToken token = createToken();
        assertThrows(VerificationTokenNotUsableException.class, () -> token.use(EXPIRES_AT));
    }

    @Test
    void shouldRejectUsingInvalidatedToken() {
        EmailVerificationToken token = createToken();
        token.invalidate(NOW);

        Instant laterButBeforeExpiration = NOW.plusSeconds(1);

        assertThrows(
                VerificationTokenNotUsableException.class,
                () -> token.use(laterButBeforeExpiration)
        );
    }

    @Test
    void shouldInvalidateToken() {
        EmailVerificationToken token = createToken();
        assertFalse(token.isInvalidated());
        token.invalidate(NOW);
        assertTrue(token.isInvalidated());
    }

    @Test
    void shouldAllowInvalidatingExpiredOutstandingToken() {
        EmailVerificationToken token = createToken();
        Instant afterExpiration = EXPIRES_AT.plusSeconds(1);

        assertTrue(token.isExpired(afterExpiration));
        assertFalse(token.isInvalidated());

        token.invalidate(afterExpiration);

        assertTrue(token.isInvalidated());
        assertEquals(afterExpiration, token.getInvalidatedAt());
    }

    @Test
    void shouldMakeInvalidationIdempotent() {
        EmailVerificationToken token = createToken();
        token.invalidate(NOW);
        token.invalidate(EXPIRES_AT);
        assertEquals(NOW, token.getInvalidatedAt());
    }


    private EmailVerificationToken createToken() {
        return EmailVerificationToken.issue(1L, "abcd", EXPIRES_AT);
    }

}
