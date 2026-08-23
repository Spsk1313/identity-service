package com.spsk1313.identityservice.identity.domain.authorization;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserAuthorizationTest {

    private static final Long USER_ID = 8L;

    @Test
    void shouldCreateUserAuthorization() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(
                                RoleName.ADMIN,
                                RoleName.USER
                        ),
                        Set.of(
                                PermissionName.USER_READ,
                                PermissionName.USER_DISABLE
                        )
                );

        assertEquals(USER_ID, authorization.getUserId());

        assertEquals(
                Set.of(
                        RoleName.ADMIN,
                        RoleName.USER
                ),
                authorization.getRoles()
        );

        assertEquals(
                Set.of(
                        PermissionName.USER_READ,
                        PermissionName.USER_DISABLE
                ),
                authorization.getPermissions()
        );
    }

    @Test
    void shouldAllowEmptyRolesAndPermissions() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(),
                        Set.of()
                );

        assertTrue(authorization.getRoles().isEmpty());
        assertTrue(authorization.getPermissions().isEmpty());
    }

    @Test
    void shouldReportWhetherUserHasRole() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(RoleName.ADMIN),
                        Set.of()
                );

        assertTrue(
                authorization.hasRole(RoleName.ADMIN)
        );

        assertFalse(
                authorization.hasRole(RoleName.USER)
        );
    }

    @Test
    void shouldReportWhetherUserHasPermission() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(RoleName.ADMIN),
                        Set.of(
                                PermissionName.USER_READ
                        )
                );

        assertTrue(
                authorization.hasPermission(
                        PermissionName.USER_READ
                )
        );

        assertFalse(
                authorization.hasPermission(
                        PermissionName.ROLE_ASSIGN
                )
        );
    }

    @Test
    void shouldRejectNullUserId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                null,
                                Set.of(),
                                Set.of()
                        )
                );

        assertEquals(
                "User id cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonPositiveUserId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                0L,
                                Set.of(),
                                Set.of()
                        )
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullRoles() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                USER_ID,
                                null,
                                Set.of()
                        )
                );

        assertEquals(
                "Roles cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectRolesContainingNull() {
        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.USER);
        roles.add(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                USER_ID,
                                roles,
                                Set.of()
                        )
                );

        assertEquals(
                "Roles cannot contain null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullPermissions() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                USER_ID,
                                Set.of(),
                                null
                        )
                );

        assertEquals(
                "Permissions cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectPermissionsContainingNull() {
        Set<PermissionName> permissions =
                new HashSet<>();

        permissions.add(PermissionName.USER_READ);
        permissions.add(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UserAuthorization.of(
                                USER_ID,
                                Set.of(RoleName.USER),
                                permissions
                        )
                );

        assertEquals(
                "Permissions cannot contain null",
                exception.getMessage()
        );
    }

    @Test
    void shouldDefensivelyCopyRolesAndPermissions() {
        Set<RoleName> roles =
                new HashSet<>(Set.of(RoleName.USER));

        Set<PermissionName> permissions =
                new HashSet<>(
                        Set.of(PermissionName.USER_READ)
                );

        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        roles,
                        permissions
                );

        roles.add(RoleName.ADMIN);
        permissions.add(
                PermissionName.ROLE_ASSIGN
        );

        assertEquals(
                Set.of(RoleName.USER),
                authorization.getRoles()
        );

        assertEquals(
                Set.of(PermissionName.USER_READ),
                authorization.getPermissions()
        );
    }

    @Test
    void shouldExposeImmutableRolesAndPermissions() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(RoleName.USER),
                        Set.of(
                                PermissionName.USER_READ
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> authorization
                        .getRoles()
                        .add(RoleName.ADMIN)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> authorization
                        .getPermissions()
                        .add(PermissionName.ROLE_ASSIGN)
        );
    }
}