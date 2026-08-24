package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;

public interface UserRoleRepository {

    UserRole save(UserRole userRole);

    boolean existsByUserIdAndRole(
            Long userId,
            RoleName role
    );
}