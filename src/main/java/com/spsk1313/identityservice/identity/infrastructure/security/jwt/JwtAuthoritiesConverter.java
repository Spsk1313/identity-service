package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities =
                new LinkedHashSet<>();

        addRoles(jwt, authorities);
        addPermissions(jwt, authorities);

        return authorities;
    }

    private void addRoles(
            Jwt jwt,
            Set<GrantedAuthority> authorities
    ) {
        List<String> roles =
                jwt.getClaimAsStringList(ROLES_CLAIM);

        if (roles == null) {
            return;
        }

        roles.stream()
                .filter(this::hasText)
                .map(role -> ROLE_PREFIX + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
    }

    private void addPermissions(
            Jwt jwt,
            Set<GrantedAuthority> authorities
    ) {
        List<String> permissions =
                jwt.getClaimAsStringList(
                        PERMISSIONS_CLAIM
                );

        if (permissions == null) {
            return;
        }

        permissions.stream()
                .filter(this::hasText)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
    }

    private boolean hasText(String value) {
        return value != null
                && !value.isBlank();
    }
}