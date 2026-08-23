package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.ForgotPasswordCommand;
import com.spsk1313.identityservice.identity.application.port.out.*;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.auth.PasswordResetToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class ForgotPasswordService {

    private static final Duration RESET_TOKEN_TTL =
            Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RawTokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final PasswordResetEmailSender emailSender;
    private final Clock clock;

    public ForgotPasswordService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RawTokenGenerator tokenGenerator,
            TokenHasher tokenHasher,
            PasswordResetEmailSender emailSender,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordCommand command) {
        User user = userRepository
                .findByEmail(
                        new EmailAddress(command.email())
                )
                .orElse(null);

        if (user == null) {
            return;
        }

        Instant now = clock.instant();

        passwordResetTokenRepository
                .invalidateAllUnusedByUserId(
                        user.getId(),
                        now
                );

        String rawToken =
                tokenGenerator.generate();

        String tokenHash =
                tokenHasher.hash(rawToken);

        PasswordResetToken token =
                PasswordResetToken.issue(
                        user.getId(),
                        tokenHash,
                        now.plus(RESET_TOKEN_TTL)
                );

        passwordResetTokenRepository.save(token);

        emailSender.sendPasswordResetEmail(
                user.getEmail().value(),
                rawToken
        );
    }
}