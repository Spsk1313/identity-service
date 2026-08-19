package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository {

    Optional<EmailVerificationToken> findOutstandingByUserId(Long userId);

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    EmailVerificationToken save(EmailVerificationToken token);
}
