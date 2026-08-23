package com.spsk1313.identityservice.identity.domain.auth;

public class PasswordResetTokenExpiredException
        extends RuntimeException {

    public PasswordResetTokenExpiredException() {
        super("Password reset token has expired");
    }
}