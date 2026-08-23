package com.spsk1313.identityservice.identity.domain.authentication;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException() {
        super("Refresh token has expired");
    }
}