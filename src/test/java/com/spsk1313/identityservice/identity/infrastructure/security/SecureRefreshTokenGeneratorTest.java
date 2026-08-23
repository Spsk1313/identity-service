package com.spsk1313.identityservice.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecureRefreshTokenGeneratorTest {

    @Test
    void shouldGenerateDifferentRefreshTokensAcrossCalls() {
        SecureRefreshTokenGenerator generator =
                new SecureRefreshTokenGenerator();

        String token1 = generator.generate();
        String token2 = generator.generate();

        assertNotEquals(token1, token2);
    }

    @Test
    void shouldGenerateUrlSafe256BitRefreshToken() {
        SecureRefreshTokenGenerator generator =
                new SecureRefreshTokenGenerator();

        String token = generator.generate();

        assertEquals(43, token.length());
        assertFalse(token.matches(".*[+/=].*"));
    }
}
