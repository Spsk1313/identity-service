package com.spsk1313.identityservice.identity.domain.auth;

import java.time.Instant;

public class AuthSession {

    private Long id;
    private Long userId;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant lastUsedAt;
    private String userAgent;

    private AuthSession(
            Long id,
            Long userId,
            Instant expiresAt,
            Instant revokedAt,
            Instant lastUsedAt,
            String userAgent
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }

        this.id = id;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.lastUsedAt = lastUsedAt;
        this.userAgent = userAgent;
    }

    public static AuthSession start(
            Long userId,
            Instant expiresAt,
            String userAgent
    ) {
        return new AuthSession(
                null,
                userId,
                expiresAt,
                null,
                null,
                userAgent
        );
    }

    public static AuthSession reconstitute(
            Long id,
            Long userId,
            Instant expiresAt,
            Instant revokedAt,
            Instant lastUsedAt,
            String userAgent
    ) {
        return new AuthSession(
                id,
                userId,
                expiresAt,
                revokedAt,
                lastUsedAt,
                userAgent
        );
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        return now.compareTo(expiresAt) >= 0;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("Revocation time cannot be null");
        }

        if (isRevoked()) {
            return;
        }

        this.revokedAt = now;
    }

    public void markUsed(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("Last used time cannot be null");
        }

        this.lastUsedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public String getUserAgent() {
        return userAgent;
    }
}