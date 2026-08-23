package com.spsk1313.identityservice.identity.domain.auth;

import java.time.Instant;

public class RefreshToken {

    private Long id;
    private Long sessionId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;

    private RefreshToken(
            Long id,
            Long sessionId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt
    ) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id cannot be null");
        }

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be null or blank");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }

        this.id = id;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public static RefreshToken issue(
            Long sessionId,
            String tokenHash,
            Instant expiresAt
    ) {
        return new RefreshToken(
                null,
                sessionId,
                tokenHash,
                expiresAt,
                null
        );
    }

    public static RefreshToken reconstitute(
            Long id,
            Long sessionId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt
    ) {
        return new RefreshToken(
                id,
                sessionId,
                tokenHash,
                expiresAt,
                usedAt
        );
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
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
            throw new IllegalArgumentException("Usage time cannot be null");
        }

        if (isUsed()) {
            throw new RefreshTokenAlreadyUsedException();
        }

        if (isExpired(now)) {
            throw new RefreshTokenExpiredException();
        }

        this.usedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
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