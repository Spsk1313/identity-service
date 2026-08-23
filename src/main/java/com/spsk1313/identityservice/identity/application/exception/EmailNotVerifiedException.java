package com.spsk1313.identityservice.identity.application.exception;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("Email verification is required before login");
    }
}