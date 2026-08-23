package com.spsk1313.identityservice.identity.domain.authorization;

import java.util.Objects;
import java.util.Set;

public class UserAuthorization {

    private final Long userId;
    private final Set<RoleName> roles;
    private final Set<PermissionName> permissions;

    private UserAuthorization(
            Long userId,
            Set<RoleName> roles,
            Set<PermissionName> permissions
    ) {
        validateUserId(userId);
        validateRoles(roles);
        validatePermissions(permissions);

        this.userId = userId;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
    }

    public static UserAuthorization of(
            Long userId,
            Set<RoleName> roles,
            Set<PermissionName> permissions
    ) {
        return new UserAuthorization(
                userId,
                roles,
                permissions
        );
    }

    public Long getUserId() {
        return userId;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public Set<PermissionName> getPermissions() {
        return permissions;
    }

    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }

    public boolean hasPermission(PermissionName permission) {
        return permissions.contains(permission);
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id cannot be null"
            );
        }

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User id must be positive"
            );
        }
    }

    private static void validateRoles(Set<RoleName> roles) {
        if (roles == null) {
            throw new IllegalArgumentException(
                    "Roles cannot be null"
            );
        }

        if (roles.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Roles cannot contain null"
            );
        }
    }

    private static void validatePermissions(
            Set<PermissionName> permissions
    ) {
        if (permissions == null) {
            throw new IllegalArgumentException(
                    "Permissions cannot be null"
            );
        }

        if (permissions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Permissions cannot contain null"
            );
        }
    }
}