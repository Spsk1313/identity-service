package com.spsk1313.identityservice.identity.domain.verification;

import java.time.Instant;
import java.util.Objects;

public class EmailVerificationToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant invalidatedAt;

    private EmailVerificationToken(
            Long id,
            Long userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt,
            Instant invalidatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.invalidatedAt = invalidatedAt;
    }

    public static EmailVerificationToken issue(
            Long userId,
            String tokenHash,
            Instant expiresAt
    ) {
        validateUserId(userId);
        validateTokenHash(tokenHash);
        Objects.requireNonNull(expiresAt, "Expiration time cannot be null");

        return new EmailVerificationToken(
                null,
                userId,
                tokenHash,
                expiresAt,
                null,
                null
        );
    }

    public static EmailVerificationToken reconstitute(
            Long id,
            Long userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt,
            Instant invalidatedAt
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Token id must be positive");
        }

        validateUserId(userId);
        validateTokenHash(tokenHash);
        Objects.requireNonNull(expiresAt, "Expiration time cannot be null");

        return new EmailVerificationToken(
                id,
                userId,
                tokenHash,
                expiresAt,
                usedAt,
                invalidatedAt
        );
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");

        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isInvalidated() {
        return invalidatedAt != null;
    }

    public boolean isUsable(Instant now) {
        return !isExpired(now)
                && !isUsed()
                && !isInvalidated();
    }

    public void use(Instant now) {
        Objects.requireNonNull(now, "Current time cannot be null");

        if (!isUsable(now)) {
            throw new VerificationTokenNotUsableException();
        }

        this.usedAt = now;
    }

    public void invalidate(Instant now) {
        Objects.requireNonNull(now, "Invalidation time cannot be null");

        if (isInvalidated() || isUsed()) {
            return;
        }

        this.invalidatedAt = now;
    }

    private static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    private static void validateTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be blank");
        }
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

    public Instant getInvalidatedAt() {
        return invalidatedAt;
    }
}