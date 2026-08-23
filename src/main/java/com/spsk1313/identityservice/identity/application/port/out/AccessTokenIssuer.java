package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;

public interface AccessTokenIssuer {

    String issue(
            Long userId,
            String email,
            UserAuthorization authorization
    );
}