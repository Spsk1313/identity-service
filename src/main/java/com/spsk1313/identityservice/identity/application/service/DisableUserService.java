package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.in.UserDisabler;
import com.spsk1313.identityservice.identity.application.port.in.UserSessionRevoker;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisableUserService implements UserDisabler {

    private final UserRepository userRepository;
    private final UserSessionRevoker userSessionRevoker;

    public DisableUserService(
            UserRepository userRepository,
            UserSessionRevoker userSessionRevoker
    ) {
        this.userRepository = userRepository;
        this.userSessionRevoker =
                userSessionRevoker;
    }

    @Override
    @Transactional
    public void disable(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User id must be positive"
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.disable();

        userRepository.save(user);

        userSessionRevoker.revokeAll(userId);
    }
}