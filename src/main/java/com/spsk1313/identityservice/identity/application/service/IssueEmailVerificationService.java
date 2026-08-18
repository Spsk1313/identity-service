package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.RawTokenGenerator;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.VerificationEmailSender;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class IssueEmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final RawTokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final VerificationEmailSender emailSender;
    private final Clock clock;
    private final String baseUrl;

    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofHours(24);

    public IssueEmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            RawTokenGenerator tokenGenerator,
            TokenHasher tokenHasher,
            VerificationEmailSender emailSender,
            Clock clock,
            @Value("${app.verification.base-url}") String baseUrl
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.emailSender = emailSender;
        this.clock = clock;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public void issue(Long userId, String email) {
        Instant now = clock.instant();

        tokenRepository.findOutstandingByUserId(userId)
                .ifPresent(token -> {
                    token.invalidate(now);
                    tokenRepository.save(token);
                });

        String rawToken = tokenGenerator.generate();

        String tokenHash = tokenHasher.hash(rawToken);

        Instant expiresAt = now.plus(VERIFICATION_TOKEN_TTL);

        EmailVerificationToken newToken = EmailVerificationToken.issue(userId, tokenHash, expiresAt);

        tokenRepository.save(newToken);

        String verificationLink = "%s/api/auth/verify-email?token=%s".formatted(baseUrl, rawToken);

        emailSender.sendVerificationEmail(email, verificationLink);
    }
}
