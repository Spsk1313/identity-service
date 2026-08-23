package com.spsk1313.identityservice.identity.domain.auth;

public class PasswordResetTokenAlreadyUsedException
        extends RuntimeException {

    public PasswordResetTokenAlreadyUsedException() {
        super("Password reset token has already been used");
    }
}