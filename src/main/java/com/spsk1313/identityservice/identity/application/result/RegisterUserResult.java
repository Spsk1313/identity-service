package com.spsk1313.identityservice.identity.application.result;

import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;

public record RegisterUserResult(
        Long id,
        String email,
        boolean emailVerified,
        AccountStatus accountStatus
) {
}
