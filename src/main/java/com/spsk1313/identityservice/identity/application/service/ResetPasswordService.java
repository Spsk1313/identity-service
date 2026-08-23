package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.ResetPasswordCommand;
import com.spsk1313.identityservice.identity.application.exception.InvalidPasswordResetTokenException;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.PasswordResetTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetToken;
import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetTokenAlreadyUsedException;
import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetTokenExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ResetPasswordService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final TokenHasher tokenHasher;
    private final PasswordHasher passwordHasher;
    private final RevokeAllAuthSessionsService revokeAllAuthSessionsService;
    private final Clock clock;

    public ResetPasswordService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            TokenHasher tokenHasher,
            PasswordHasher passwordHasher,
            RevokeAllAuthSessionsService revokeAllAuthSessionsService,
            Clock clock
    ) {
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
        this.passwordHasher = passwordHasher;
        this.revokeAllAuthSessionsService =
                revokeAllAuthSessionsService;
        this.clock = clock;
    }

    @Transactional
    public void resetPassword(
            ResetPasswordCommand command
    ) {
        Instant now = clock.instant();

        String tokenHash =
                tokenHasher.hash(command.token());

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                InvalidPasswordResetTokenException::new
                        );

        try {
            resetToken.use(now);
        } catch (
                PasswordResetTokenExpiredException |
                PasswordResetTokenAlreadyUsedException ex
        ) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = userRepository
                .findById(resetToken.getUserId())
                .orElseThrow(
                        InvalidPasswordResetTokenException::new
                );

        String newPasswordHash =
                passwordHasher.hash(
                        command.newPassword()
                );

        user.changePassword(newPasswordHash);

        userRepository.save(user);

        passwordResetTokenRepository.save(resetToken);

        revokeAllAuthSessionsService.revokeAll(
                user.getId()
        );
    }
}