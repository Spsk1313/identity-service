package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.in.UserRoleAssigner;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignUserRoleService
        implements UserRoleAssigner {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public AssignUserRoleService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void assign(
            Long userId,
            RoleName role
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User id must be positive"
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userRoleRepository.existsByUserIdAndRole(
                userId,
                role
        )) {
            return;
        }

        UserRole userRole =
                UserRole.assign(
                        userId,
                        role
                );

        userRoleRepository.save(userRole);
    }
}