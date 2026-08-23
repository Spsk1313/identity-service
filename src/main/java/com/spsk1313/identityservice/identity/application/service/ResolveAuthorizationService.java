package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.in.AuthorizationResolver;
import com.spsk1313.identityservice.identity.application.port.out.UserAuthorizationRepository;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResolveAuthorizationService
        implements AuthorizationResolver {

    private final UserAuthorizationRepository userAuthorizationRepository;

    public ResolveAuthorizationService(
            UserAuthorizationRepository userAuthorizationRepository
    ) {
        this.userAuthorizationRepository =
                userAuthorizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAuthorization resolve(Long userId) {
        return userAuthorizationRepository.findByUserId(userId);
    }
}