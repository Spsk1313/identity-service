package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;

public interface UserAuthorizationRepository {

    UserAuthorization findByUserId(Long userId);
}