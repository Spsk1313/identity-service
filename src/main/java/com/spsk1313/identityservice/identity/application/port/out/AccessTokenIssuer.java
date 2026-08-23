package com.spsk1313.identityservice.identity.application.port.out;

public interface AccessTokenIssuer {

    String issue(
            Long userId,
            String email
    );
}