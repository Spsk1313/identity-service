package com.spsk1313.identityservice.identity.application.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {

        super("This email is already in use");
    }
}
