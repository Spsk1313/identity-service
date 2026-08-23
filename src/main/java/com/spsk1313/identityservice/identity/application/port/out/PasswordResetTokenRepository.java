package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void invalidateAllUnusedByUserId(
            Long userId,
            Instant usedAt
    );
}