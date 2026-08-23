package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.domain.authorization.PermissionName;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
import com.spsk1313.identityservice.identity.infrastructure.persistence.projection.UserAuthorizationProjection;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserAuthorizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserAuthorizationRepositoryAdapterTest {

    @Mock
    private SpringDataUserAuthorizationRepository repository;

    private JpaUserAuthorizationRepositoryAdapter adapter;

    private static final Long USER_ID = 8L;

    @BeforeEach
    void setUp() {
        adapter = new JpaUserAuthorizationRepositoryAdapter(
                repository
        );
    }

    @Test
    void shouldResolveRoleAndPermissionsForUser() {
        UserAuthorizationProjection first =
                projection("ADMIN", "USER_READ");

        UserAuthorizationProjection second =
                projection("ADMIN", "USER_DISABLE");

        UserAuthorizationProjection third =
                projection("ADMIN", "SESSION_REVOKE");

        UserAuthorizationProjection fourth =
                projection("ADMIN", "ROLE_ASSIGN");

        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of(
                        first,
                        second,
                        third,
                        fourth
                ));

        UserAuthorization result =
                adapter.findByUserId(USER_ID);

        assertEquals(USER_ID, result.getUserId());

        assertEquals(
                Set.of(RoleName.ADMIN),
                result.getRoles()
        );

        assertEquals(
                Set.of(
                        PermissionName.USER_READ,
                        PermissionName.USER_DISABLE,
                        PermissionName.SESSION_REVOKE,
                        PermissionName.ROLE_ASSIGN
                ),
                result.getPermissions()
        );

        verify(repository)
                .findAuthorizationByUserId(USER_ID);
    }

    @Test
    void shouldResolveRoleWithNoPermissions() {
        UserAuthorizationProjection row =
                projection("USER", null);

        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of(row));

        UserAuthorization result =
                adapter.findByUserId(USER_ID);

        assertEquals(
                Set.of(RoleName.USER),
                result.getRoles()
        );

        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void shouldResolveMultipleRolesAndDeduplicatePermissions() {
        UserAuthorizationProjection first =
                projection(
                        "ADMIN",
                        "USER_READ"
                );

        UserAuthorizationProjection second =
                projection(
                        "ADMIN",
                        "SESSION_REVOKE"
                );

        UserAuthorizationProjection third =
                projection(
                        "MODERATOR",
                        "USER_READ"
                );

        UserAuthorizationProjection fourth =
                projection(
                        "MODERATOR",
                        "SESSION_REVOKE"
                );

        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of(
                        first,
                        second,
                        third,
                        fourth
                ));

        UserAuthorization result =
                adapter.findByUserId(USER_ID);

        assertEquals(
                Set.of(
                        RoleName.ADMIN,
                        RoleName.MODERATOR
                ),
                result.getRoles()
        );

        assertEquals(
                Set.of(
                        PermissionName.USER_READ,
                        PermissionName.SESSION_REVOKE
                ),
                result.getPermissions()
        );
    }

    @Test
    void shouldReturnEmptyAuthorizationWhenUserHasNoRoles() {
        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of());

        UserAuthorization result =
                adapter.findByUserId(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void shouldRejectNullUserId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> adapter.findByUserId(null)
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNonPositiveUserId() {
        IllegalArgumentException zeroException =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> adapter.findByUserId(0L)
                );

        assertEquals(
                "User id must be positive",
                zeroException.getMessage()
        );

        IllegalArgumentException negativeException =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> adapter.findByUserId(-1L)
                );

        assertEquals(
                "User id must be positive",
                negativeException.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldFailWhenDatabaseContainsUnknownRole() {
        UserAuthorizationProjection row =
                projection(
                        "SUPER_ADMIN",
                        null
                );

        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of(row));

        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.findByUserId(USER_ID)
        );
    }

    @Test
    void shouldFailWhenDatabaseContainsUnknownPermission() {
        UserAuthorizationProjection row =
                projection(
                        "ADMIN",
                        "DELETE_EVERYTHING"
                );

        when(repository.findAuthorizationByUserId(USER_ID))
                .thenReturn(List.of(row));

        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.findByUserId(USER_ID)
        );
    }

    private UserAuthorizationProjection projection(
            String role,
            String permission
    ) {
        UserAuthorizationProjection projection =
                mock(UserAuthorizationProjection.class);

        when(projection.getRoleName())
                .thenReturn(role);

        if (permission != null) {
            when(projection.getPermissionName())
                    .thenReturn(permission);
        }

        return projection;
    }
}