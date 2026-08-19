package com.spsk1313.identityservice.identity.application.policy;

import com.spsk1313.identityservice.identity.application.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void shouldRejectNullPassword() {
        assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate(null)
        );
    }

    @Test
    void shouldRejectPasswordShorterThanMinimumLength() {
        assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate("a".repeat(11))
        );
    }

    @Test
    void shouldAcceptPasswordWithinAllowedLength() {
        assertDoesNotThrow(
                () -> passwordPolicy.validate("a".repeat(12))
        );
    }

    @Test
    void shouldRejectPasswordLongerThanMaximumLength() {
        assertThrows(
                InvalidPasswordException.class,
                () -> passwordPolicy.validate("a".repeat(65))
        );
    }
}