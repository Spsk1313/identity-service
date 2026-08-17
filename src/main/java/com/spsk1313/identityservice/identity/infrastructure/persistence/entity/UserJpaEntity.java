package com.spsk1313.identityservice.identity.infrastructure.persistence.entity;

import com.spsk1313.identityservice.identity.domain.AccountStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_PASSWORD_HASH_LENGTH = 255;
    private static final int MAX_ACCOUNT_STATUS_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(name = "password_hash", nullable = false, length = MAX_PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = MAX_ACCOUNT_STATUS_LENGTH)
    private AccountStatus accountStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected UserJpaEntity(){}

    public UserJpaEntity(
            Long id,
            String email,
            String passwordHash,
            boolean emailVerified,
            AccountStatus accountStatus
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.accountStatus = accountStatus;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
