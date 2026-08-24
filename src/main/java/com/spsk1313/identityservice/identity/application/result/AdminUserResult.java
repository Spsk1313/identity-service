package com.spsk1313.identityservice.identity.application.result;

import com.spsk1313.identityservice.identity.domain.AccountStatus;

public record AdminUserResult(
        Long id,
        String email,
        boolean emailVerified,
        AccountStatus accountStatus
) {
}