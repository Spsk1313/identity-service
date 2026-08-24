package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.in.AdminUserReader;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.result.AdminUserResult;
import com.spsk1313.identityservice.identity.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminUserService implements AdminUserReader {

    private final UserRepository userRepository;

    public GetAdminUserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResult getById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User id must be positive"
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return new AdminUserResult(
                user.getId(),
                user.getEmail().value(),
                user.isEmailVerified(),
                user.getAccountStatus()
        );
    }
}