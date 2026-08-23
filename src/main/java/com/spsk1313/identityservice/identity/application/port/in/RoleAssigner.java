package com.spsk1313.identityservice.identity.application.port.in;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;

public interface RoleAssigner {

    void assign(Long userId, RoleName role);
}