package com.spsk1313.identityservice.identity.domain.verification;

public class VerificationTokenNotUsableException extends RuntimeException {

    public VerificationTokenNotUsableException() {
        super("Verification token is not usable");
    }
}