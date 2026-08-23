package com.spsk1313.identityservice.identity.domain.authorization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    private static final Long USER_ID = 8L;

    @Test
    void shouldAssignValidRoleToUser() {
        UserRole userRole = UserRole.assign(
                USER_ID,
                RoleName.USER
        );

        assertEquals(USER_ID, userRole.getUserId());
        assertEquals(RoleName.USER, userRole.getRole());
    }

    @Test
    void shouldReconstituteExistingUserRole() {
        UserRole userRole = UserRole.reconstitute(
                USER_ID,
                RoleName.ADMIN
        );

        assertEquals(USER_ID, userRole.getUserId());
        assertEquals(RoleName.ADMIN, userRole.getRole());
    }

    @Test
    void shouldRejectNullUserIdWhenAssigningRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.assign(
                                null,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectZeroUserIdWhenAssigningRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.assign(
                                0L,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNegativeUserIdWhenAssigningRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.assign(
                                -1L,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullRoleWhenAssigningRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.assign(
                                USER_ID,
                                null
                        )
                );

        assertEquals(
                "Role cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullUserIdWhenReconstituting() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.reconstitute(
                                null,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonPositiveUserIdWhenReconstituting() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.reconstitute(
                                0L,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullRoleWhenReconstituting() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserRole.reconstitute(
                                USER_ID,
                                null
                        )
                );

        assertEquals(
                "Role cannot be null",
                exception.getMessage()
        );
    }
}