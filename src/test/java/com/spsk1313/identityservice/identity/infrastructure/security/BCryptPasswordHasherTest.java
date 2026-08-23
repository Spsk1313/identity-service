package com.spsk1313.identityservice.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private static final String PASSWORD =
            "correct-horse-battery-staple";

    @Test
    void shouldMatchRawPasswordAgainstStoredHash() {
        BCryptPasswordHasher passwordHasher =
                new BCryptPasswordHasher(
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12)
                );

        String passwordHash =
                passwordHasher.hash(PASSWORD);

        assertTrue(
                passwordHasher.matches(
                        PASSWORD,
                        passwordHash
                )
        );

        assertFalse(
                passwordHasher.matches(
                        "wrong-password",
                        passwordHash
                )
        );
    }
}