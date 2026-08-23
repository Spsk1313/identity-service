package com.spsk1313.identityservice.identity.application.port.in;

import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;

public interface AuthorizationResolver {

    UserAuthorization resolve(Long userId);
}