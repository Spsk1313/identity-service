package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.ResetPasswordCommand;
import com.spsk1313.identityservice.identity.application.exception.InvalidPasswordResetTokenException;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.PasswordResetTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    private static final Long USER_ID = 8L;

    private static final String RAW_TOKEN =
            "raw-password-reset-token";

    private static final String TOKEN_HASH =
            "hashed-password-reset-token";

    private static final String NEW_PASSWORD =
            "new-super-secret-password";

    private static final String NEW_PASSWORD_HASH =
            "hashed-new-password";

    private static final Instant NOW =
            Instant.parse("2026-08-23T20:00:00Z");

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private RevokeAllAuthSessionsService revokeAllAuthSessionsService;

    private ResetPasswordService resetPasswordService;

    @BeforeEach
    void setUp() {
        Clock clock =
                Clock.fixed(NOW, ZoneOffset.UTC);

        resetPasswordService =
                new ResetPasswordService(
                        passwordResetTokenRepository,
                        userRepository,
                        tokenHasher,
                        passwordHasher,
                        revokeAllAuthSessionsService,
                        clock
                );
    }

    @Test
    void shouldResetPasswordConsumeTokenAndRevokeAllSessions() {
        PasswordResetToken resetToken =
                validResetToken();

        User user = mock(User.class);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(resetToken));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordHasher.hash(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        when(user.getId())
                .thenReturn(USER_ID);

        ResetPasswordCommand command =
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                );

        resetPasswordService.resetPassword(command);

        assertTrue(resetToken.isUsed());

        assertEquals(
                NOW,
                resetToken.getUsedAt()
        );

        verify(tokenHasher)
                .hash(RAW_TOKEN);

        verify(passwordHasher)
                .hash(NEW_PASSWORD);

        verify(user)
                .changePassword(NEW_PASSWORD_HASH);

        verify(userRepository)
                .save(user);

        verify(passwordResetTokenRepository)
                .save(resetToken);

        verify(revokeAllAuthSessionsService)
                .revokeAll(USER_ID);
    }

    @Test
    void shouldRejectUnknownResetToken() {
        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        ResetPasswordCommand command =
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                );

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> resetPasswordService
                        .resetPassword(command)
        );

        verifyNoInteractions(
                userRepository,
                passwordHasher,
                revokeAllAuthSessionsService
        );

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldRejectExpiredResetToken() {
        PasswordResetToken expiredToken =
                PasswordResetToken.reconstitute(
                        10L,
                        USER_ID,
                        TOKEN_HASH,
                        NOW.minusSeconds(1),
                        null
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(expiredToken));

        ResetPasswordCommand command =
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                );

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> resetPasswordService
                        .resetPassword(command)
        );

        assertFalse(expiredToken.isUsed());
        assertNull(expiredToken.getUsedAt());

        verifyNoInteractions(
                userRepository,
                passwordHasher,
                revokeAllAuthSessionsService
        );

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldRejectResetTokenAtExactExpirationTime() {
        PasswordResetToken expiredToken =
                PasswordResetToken.reconstitute(
                        10L,
                        USER_ID,
                        TOKEN_HASH,
                        NOW,
                        null
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> resetPasswordService.resetPassword(
                        new ResetPasswordCommand(
                                RAW_TOKEN,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoInteractions(
                userRepository,
                passwordHasher,
                revokeAllAuthSessionsService
        );
    }

    @Test
    void shouldRejectAlreadyUsedResetToken() {
        Instant originallyUsedAt =
                NOW.minusSeconds(300);

        PasswordResetToken usedToken =
                PasswordResetToken.reconstitute(
                        10L,
                        USER_ID,
                        TOKEN_HASH,
                        NOW.plusSeconds(600),
                        originallyUsedAt
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(usedToken));

        ResetPasswordCommand command =
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                );

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> resetPasswordService
                        .resetPassword(command)
        );

        assertEquals(
                originallyUsedAt,
                usedToken.getUsedAt()
        );

        verifyNoInteractions(
                userRepository,
                passwordHasher,
                revokeAllAuthSessionsService
        );

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldRejectResetWhenTokenReferencesMissingUser() {
        PasswordResetToken resetToken =
                validResetToken();

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(resetToken));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> resetPasswordService.resetPassword(
                        new ResetPasswordCommand(
                                RAW_TOKEN,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoInteractions(
                passwordHasher,
                revokeAllAuthSessionsService
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldHashRawPasswordBeforeChangingUserPassword() {
        PasswordResetToken resetToken =
                validResetToken();

        User user = mock(User.class);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(resetToken));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordHasher.hash(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        when(user.getId())
                .thenReturn(USER_ID);

        resetPasswordService.resetPassword(
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                )
        );

        verify(passwordHasher)
                .hash(NEW_PASSWORD);

        verify(user)
                .changePassword(NEW_PASSWORD_HASH);

        verify(user, never())
                .changePassword(NEW_PASSWORD);
    }

    @Test
    void shouldPersistConsumedTokenWithCurrentTimestamp() {
        PasswordResetToken resetToken =
                validResetToken();

        User user = mock(User.class);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(resetToken));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordHasher.hash(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        when(user.getId())
                .thenReturn(USER_ID);

        resetPasswordService.resetPassword(
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                )
        );

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );

        verify(passwordResetTokenRepository)
                .save(captor.capture());

        PasswordResetToken savedToken =
                captor.getValue();

        assertTrue(savedToken.isUsed());
        assertEquals(NOW, savedToken.getUsedAt());
    }

    @Test
    void shouldPerformCredentialChangesBeforeRevokingSessions() {
        PasswordResetToken resetToken =
                validResetToken();

        User user = mock(User.class);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(passwordResetTokenRepository
                .findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(resetToken));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordHasher.hash(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        when(user.getId())
                .thenReturn(USER_ID);

        resetPasswordService.resetPassword(
                new ResetPasswordCommand(
                        RAW_TOKEN,
                        NEW_PASSWORD
                )
        );

        InOrder inOrder = inOrder(
                userRepository,
                passwordResetTokenRepository,
                revokeAllAuthSessionsService
        );

        inOrder.verify(passwordResetTokenRepository)
                .findByTokenHash(TOKEN_HASH);

        inOrder.verify(userRepository)
                .findById(USER_ID);

        inOrder.verify(userRepository)
                .save(user);

        inOrder.verify(passwordResetTokenRepository)
                .save(resetToken);

        inOrder.verify(revokeAllAuthSessionsService)
                .revokeAll(USER_ID);
    }

    private PasswordResetToken validResetToken() {
        return PasswordResetToken.reconstitute(
                10L,
                USER_ID,
                TOKEN_HASH,
                NOW.plusSeconds(1800),
                null
        );
    }
}