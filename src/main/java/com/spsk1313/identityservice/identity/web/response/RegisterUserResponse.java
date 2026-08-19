package com.spsk1313.identityservice.identity.web.response;

import com.spsk1313.identityservice.identity.domain.AccountStatus;

public record RegisterUserResponse(
        Long id,
        String email,
        boolean emailVerified,
        AccountStatus accountStatus
) {
}
