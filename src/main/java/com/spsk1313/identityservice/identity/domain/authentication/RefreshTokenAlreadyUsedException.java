package com.spsk1313.identityservice.identity.domain.authentication;

public class RefreshTokenAlreadyUsedException extends RuntimeException {

    public RefreshTokenAlreadyUsedException() {
        super("Refresh token has already been used");
    }
}