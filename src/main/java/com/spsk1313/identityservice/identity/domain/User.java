package com.spsk1313.identityservice.identity.domain;

public class User {

    private Long id;
    private EmailAddress email;
    private String passwordHash;
    private boolean emailVerified;
    private AccountStatus accountStatus;

    private User(
            Long id,
            EmailAddress email,
            String passwordHash,
            boolean emailVerified,
            AccountStatus accountStatus
    ) {
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateAccountStatus(accountStatus);
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.accountStatus = accountStatus;
    }

    public static User register(EmailAddress email, String passwordHash) {
        return new User(
                null,
                email,
                passwordHash,
                false,
                AccountStatus.ACTIVE
        );
    }

    public static User reconstitute(
            Long id,
            EmailAddress email,
            String passwordHash,
            boolean emailVerified,
            AccountStatus accountStatus
    ) {
        validateId(id);
        return new User(
                id,
                email,
                passwordHash,
                emailVerified,
                accountStatus
        );
    }

    public void verifyEmail() {
        if (emailVerified) {
            return;
        }

        this.emailVerified = true;
    }

    public void enable() {
        if (accountStatus == AccountStatus.ACTIVE) {
            return;
        }

        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void disable() {
        if (accountStatus == AccountStatus.DISABLED) {
            return;
        }

        this.accountStatus = AccountStatus.DISABLED;
    }

    public Long getId() {
        return id;
    }

    public EmailAddress getEmail() {
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

    private static void validateId(Long id) {
        if(id == null) throw new IllegalArgumentException("Id cannot be null");
        if(id <= 0) throw new IllegalArgumentException("Id must be positive");
    }
    private static void validateEmail(EmailAddress email) {
        if(email == null) throw new IllegalArgumentException("Email cannot be null");
    }

    private static void validatePasswordHash(String passwordHash) {
        if(passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password hash cannot be null or blank.");
    }

    private static void validateAccountStatus(AccountStatus accountStatus) {
        if(accountStatus == null) throw new IllegalArgumentException("Account status cannot be null");
    }
}