package com.spsk1313.identityservice.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SecureRandomTokenGeneratorTest {

    @Test
    void shouldGenerateDifferentTokensAcrossCalls() {
        SecureRandomTokenGenerator generator = new SecureRandomTokenGenerator();

        String token1 = generator.generate();
        String token2 = generator.generate();

        assertNotEquals(token1,token2);
    }

    @Test
    void shouldGenerateUrlSafeToken() {
        SecureRandomTokenGenerator generator = new SecureRandomTokenGenerator();

        String token = generator.generate();

        assertFalse(token.matches(".*[+=/].*"));
    }
}
