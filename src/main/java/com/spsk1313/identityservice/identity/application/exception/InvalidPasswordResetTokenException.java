package com.spsk1313.identityservice.identity.application.exception;

public class InvalidPasswordResetTokenException
        extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("Invalid or expired password reset token");
    }
}