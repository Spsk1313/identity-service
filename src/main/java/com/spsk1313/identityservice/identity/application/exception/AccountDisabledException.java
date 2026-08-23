package com.spsk1313.identityservice.identity.application.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("This account is disabled");
    }
}