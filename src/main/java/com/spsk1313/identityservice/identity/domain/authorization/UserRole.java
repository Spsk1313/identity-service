package com.spsk1313.identityservice.identity.domain.authorization;

public class UserRole {

    private final Long userId;
    private final RoleName role;

    private UserRole(
            Long userId,
            RoleName role
    ) {
        validateUserId(userId);
        validateRole(role);

        this.userId = userId;
        this.role = role;
    }

    public static UserRole assign(
            Long userId,
            RoleName role
    ) {
        return new UserRole(
                userId,
                role
        );
    }

    public static UserRole reconstitute(
            Long userId,
            RoleName role
    ) {
        return new UserRole(
                userId,
                role
        );
    }

    public Long getUserId() {
        return userId;
    }

    public RoleName getRole() {
        return role;
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

    private static void validateRole(RoleName role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }
    }
}