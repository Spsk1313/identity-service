package com.spsk1313.identityservice.identity.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final EmailAddress EMAIL =
            new EmailAddress("sahil@example.com");

    private static final String PASSWORD_HASH =
            "some-valid-password-hash";

    @Test
    void shouldRegisterUserWithDefaultState() {
        User user = User.register(EMAIL, PASSWORD_HASH);

        assertNull(user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD_HASH, user.getPasswordHash());
        assertFalse(user.isEmailVerified());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void shouldVerifyEmail() {
        User user = User.register(EMAIL, PASSWORD_HASH);

        assertFalse(user.isEmailVerified());

        user.verifyEmail();

        assertTrue(user.isEmailVerified());
    }

    @Test
    void shouldMakeEmailVerificationIdempotent() {
        User user = User.register(EMAIL, PASSWORD_HASH);
        user.verifyEmail();
        user.verifyEmail();
        assertTrue(user.isEmailVerified());
    }

    @Test
    void shouldDisableUser() {
        User user = User.register(EMAIL, PASSWORD_HASH);

        user.disable();

        assertEquals(AccountStatus.DISABLED, user.getAccountStatus());
    }

    @Test
    void shouldEnableDisabledUser() {
        User user = User.register(EMAIL, PASSWORD_HASH);
        user.disable();

        user.enable();

        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void shouldReconstituteExistingUserWithPersistedState() {
        User user = User.reconstitute(
                42L,
                EMAIL,
                PASSWORD_HASH,
                true,
                AccountStatus.DISABLED
        );

        assertEquals(42L, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD_HASH, user.getPasswordHash());
        assertTrue(user.isEmailVerified());
        assertEquals(AccountStatus.DISABLED, user.getAccountStatus());
    }

    @Test
    void shouldRejectNullEmailWhenRegistering() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.register(null, PASSWORD_HASH)
        );
    }

    @Test
    void shouldRejectNullPasswordHashWhenRegistering() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.register(EMAIL, null)
        );
    }

    @Test
    void shouldRejectBlankPasswordHashWhenRegistering() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.register(EMAIL, "   ")
        );
    }

    @Test
    void shouldRejectNullIdWhenReconstituting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.reconstitute(
                        null,
                        EMAIL,
                        PASSWORD_HASH,
                        false,
                        AccountStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldRejectZeroIdWhenReconstituting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.reconstitute(
                        0L,
                        EMAIL,
                        PASSWORD_HASH,
                        false,
                        AccountStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldRejectNegativeIdWhenReconstituting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.reconstitute(
                        -1L,
                        EMAIL,
                        PASSWORD_HASH,
                        false,
                        AccountStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldRejectNullAccountStatusWhenReconstituting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.reconstitute(
                        42L,
                        EMAIL,
                        PASSWORD_HASH,
                        false,
                        null
                )
        );
    }
}