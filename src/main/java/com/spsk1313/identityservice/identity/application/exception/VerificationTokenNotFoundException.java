package com.spsk1313.identityservice.identity.application.exception;

public class VerificationTokenNotFoundException extends RuntimeException {

    public VerificationTokenNotFoundException() {
        super("Verification token was not found");
    }
}