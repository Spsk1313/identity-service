package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthoritiesConverterTest {

    private JwtAuthoritiesConverter converter;

    @BeforeEach
    void setUp() {
        converter =
                new JwtAuthoritiesConverter();
    }

    @Test
    void shouldConvertRoleToSpringRoleAuthority() {
        Jwt jwt =
                jwt(
                        List.of("ADMIN"),
                        List.of()
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                Set.of("ROLE_ADMIN"),
                authorityNames(authorities)
        );
    }

    @Test
    void shouldConvertMultipleRolesToSpringRoleAuthorities() {
        Jwt jwt =
                jwt(
                        List.of(
                                "ADMIN",
                                "MODERATOR"
                        ),
                        List.of()
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                Set.of(
                        "ROLE_ADMIN",
                        "ROLE_MODERATOR"
                ),
                authorityNames(authorities)
        );
    }

    @Test
    void shouldConvertPermissionsWithoutAddingRolePrefix() {
        Jwt jwt =
                jwt(
                        List.of(),
                        List.of(
                                "USER_READ",
                                "USER_DISABLE"
                        )
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_DISABLE"
                ),
                authorityNames(authorities)
        );
    }

    @Test
    void shouldCombineRolesAndPermissions() {
        Jwt jwt =
                jwt(
                        List.of("ADMIN"),
                        List.of(
                                "USER_READ",
                                "USER_DISABLE",
                                "SESSION_REVOKE",
                                "ROLE_ASSIGN"
                        )
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                Set.of(
                        "ROLE_ADMIN",
                        "USER_READ",
                        "USER_DISABLE",
                        "SESSION_REVOKE",
                        "ROLE_ASSIGN"
                ),
                authorityNames(authorities)
        );
    }

    @Test
    void shouldDeduplicateAuthorities() {
        Jwt jwt =
                jwt(
                        List.of(
                                "ADMIN",
                                "ADMIN"
                        ),
                        List.of(
                                "USER_READ",
                                "USER_READ"
                        )
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                2,
                authorities.size()
        );

        assertEquals(
                Set.of(
                        "ROLE_ADMIN",
                        "USER_READ"
                ),
                authorityNames(authorities)
        );
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenClaimsAreMissing() {
        Jwt jwt =
                jwtWithoutAuthorizationClaims();

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenClaimsAreEmpty() {
        Jwt jwt =
                jwt(
                        List.of(),
                        List.of()
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void shouldIgnoreBlankRoleAndPermissionValues() {
        Jwt jwt =
                jwt(
                        List.of(
                                "ADMIN",
                                "",
                                " "
                        ),
                        List.of(
                                "USER_READ",
                                "",
                                " "
                        )
                );

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertEquals(
                Set.of(
                        "ROLE_ADMIN",
                        "USER_READ"
                ),
                authorityNames(authorities)
        );
    }

    private Jwt jwt(
            List<String> roles,
            List<String> permissions
    ) {
        Instant now =
                Instant.parse(
                        "2026-08-23T12:00:00Z"
                );

        return new Jwt(
                "test-token",
                now,
                now.plusSeconds(900),
                Map.of(
                        "alg",
                        "RS256"
                ),
                Map.of(
                        "sub",
                        "42",
                        "roles",
                        roles,
                        "permissions",
                        permissions
                )
        );
    }

    private Jwt jwtWithoutAuthorizationClaims() {
        Instant now =
                Instant.parse(
                        "2026-08-23T12:00:00Z"
                );

        return new Jwt(
                "test-token",
                now,
                now.plusSeconds(900),
                Map.of(
                        "alg",
                        "RS256"
                ),
                Map.of(
                        "sub",
                        "42"
                )
        );
    }

    private Set<String> authorityNames(
            Collection<GrantedAuthority> authorities
    ) {
        return authorities.stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .collect(Collectors.toSet());
    }
}