package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.in.RoleAssigner;
import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignRoleService implements RoleAssigner {

    private final UserRoleRepository userRoleRepository;

    public AssignRoleService(
            UserRoleRepository userRoleRepository
    ) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void assign(
            Long userId,
            RoleName role
    ) {
        UserRole userRole =
                UserRole.assign(userId, role);

        userRoleRepository.save(userRole);
    }
}