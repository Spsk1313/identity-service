package com.spsk1313.identityservice.identity.application.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User was not found");
    }
}