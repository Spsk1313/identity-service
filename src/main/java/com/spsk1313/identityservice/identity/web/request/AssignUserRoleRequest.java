package com.spsk1313.identityservice.identity.web.request;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import jakarta.validation.constraints.NotNull;

public record AssignUserRoleRequest(
        @NotNull RoleName role
) {
}