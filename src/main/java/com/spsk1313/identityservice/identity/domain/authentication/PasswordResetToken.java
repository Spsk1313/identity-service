package com.spsk1313.identityservice.identity.domain.authentication;

import java.time.Instant;

public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;

    private PasswordResetToken(
            Long id,
            Long userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Token hash cannot be null or blank"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Expiration time cannot be null"
            );
        }

        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public static PasswordResetToken issue(
            Long userId,
            String tokenHash,
            Instant expiresAt
    ) {
        return new PasswordResetToken(
                null,
                userId,
                tokenHash,
                expiresAt,
                null
        );
    }

    public static PasswordResetToken reconstitute(
            Long id,
            Long userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt
    ) {
        return new PasswordResetToken(
                id,
                userId,
                tokenHash,
                expiresAt,
                usedAt
        );
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException(
                    "Current time cannot be null"
            );
        }

        return now.compareTo(expiresAt) >= 0;
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isUsable(Instant now) {
        return !isExpired(now) && !isUsed();
    }

    public void use(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException(
                    "Usage time cannot be null"
            );
        }

        if (isUsed()) {
            throw new PasswordResetTokenAlreadyUsedException();
        }

        if (isExpired(now)) {
            throw new PasswordResetTokenExpiredException();
        }

        this.usedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}