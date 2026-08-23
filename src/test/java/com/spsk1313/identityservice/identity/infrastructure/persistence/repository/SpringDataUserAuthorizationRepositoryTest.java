package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.projection.UserAuthorizationProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
class SpringDataUserAuthorizationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private SpringDataUserAuthorizationRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldResolveUserRoleWithNoPermissions() {
        Long userId =
                createUser(
                        "authorization-user@example.com"
                );

        Long roleId =
                findRoleId("USER");

        assignRole(
                userId,
                roleId
        );

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(
                        userId
                );

        assertEquals(
                1,
                rows.size()
        );

        UserAuthorizationProjection row =
                rows.getFirst();

        assertEquals(
                "USER",
                row.getRoleName()
        );

        assertNull(
                row.getPermissionName()
        );
    }

    @Test
    void shouldResolveModeratorPermissions() {
        Long userId =
                createUser(
                        "authorization-moderator@example.com"
                );

        Long roleId =
                findRoleId("MODERATOR");

        assignRole(
                userId,
                roleId
        );

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(
                        userId
                );

        Set<String> roles =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getRoleName
                        )
                        .collect(Collectors.toSet());

        Set<String> permissions =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getPermissionName
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of("MODERATOR"),
                roles
        );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "SESSION_REVOKE"
                ),
                permissions
        );
    }

    @Test
    void shouldResolveAdminPermissions() {
        Long userId =
                createUser(
                        "authorization-admin@example.com"
                );

        Long roleId =
                findRoleId("ADMIN");

        assignRole(
                userId,
                roleId
        );

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(
                        userId
                );

        Set<String> roles =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getRoleName
                        )
                        .collect(Collectors.toSet());

        Set<String> permissions =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getPermissionName
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of("ADMIN"),
                roles
        );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_DISABLE",
                        "SESSION_REVOKE",
                        "ROLE_ASSIGN"
                ),
                permissions
        );
    }

    @Test
    void shouldResolveAuthorizationAcrossMultipleRoles() {
        Long userId =
                createUser(
                        "authorization-multi@example.com"
                );

        Long adminRoleId =
                findRoleId("ADMIN");

        Long moderatorRoleId =
                findRoleId("MODERATOR");

        assignRole(
                userId,
                adminRoleId
        );

        assignRole(
                userId,
                moderatorRoleId
        );

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(
                        userId
                );

        Set<String> roles =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getRoleName
                        )
                        .collect(Collectors.toSet());

        Set<String> permissions =
                rows.stream()
                        .map(
                                UserAuthorizationProjection::getPermissionName
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        "ADMIN",
                        "MODERATOR"
                ),
                roles
        );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_DISABLE",
                        "SESSION_REVOKE",
                        "ROLE_ASSIGN"
                ),
                permissions
        );
    }

    @Test
    void shouldReturnNoRowsWhenUserHasNoRoles() {
        Long userId =
                createUser(
                        "authorization-no-role@example.com"
                );

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(
                        userId
                );

        assertTrue(rows.isEmpty());
    }

    private Long createUser(String email) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO users (
                    email,
                    password_hash,
                    email_verified,
                    account_status,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?,
                    ?,
                    false,
                    'ACTIVE',
                    NOW(),
                    NOW()
                )
                RETURNING id
                """,
                Long.class,
                email,
                "test-password-hash"
        );
    }

    private Long findRoleId(String roleName) {
        return jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM roles
                WHERE name = ?
                """,
                Long.class,
                roleName
        );
    }

    private void assignRole(
            Long userId,
            Long roleId
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO user_roles (
                    user_id,
                    role_id
                )
                VALUES (?, ?)
                """,
                userId,
                roleId
        );
    }
}