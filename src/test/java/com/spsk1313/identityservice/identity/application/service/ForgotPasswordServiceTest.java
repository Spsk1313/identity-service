package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.ForgotPasswordCommand;
import com.spsk1313.identityservice.identity.application.port.out.PasswordResetEmailSender;
import com.spsk1313.identityservice.identity.application.port.out.PasswordResetTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.RawTokenGenerator;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.auth.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-23T19:00:00Z");

    private static final Long USER_ID = 8L;

    private static final String EMAIL =
            "login-test@example.com";

    private static final String RAW_TOKEN =
            "raw-password-reset-token";

    private static final String TOKEN_HASH =
            "hashed-password-reset-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RawTokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private PasswordResetEmailSender emailSender;

    private ForgotPasswordService forgotPasswordService;

    @BeforeEach
    void setUp() {
        Clock clock =
                Clock.fixed(NOW, ZoneOffset.UTC);

        forgotPasswordService =
                new ForgotPasswordService(
                        userRepository,
                        passwordResetTokenRepository,
                        tokenGenerator,
                        tokenHasher,
                        emailSender,
                        clock
                );
    }

    @Test
    void shouldIssuePasswordResetTokenAndSendEmailForExistingUser() {
        User user = mock(User.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getEmail())
                .thenReturn(new EmailAddress(EMAIL));

        when(userRepository.findByEmail(new EmailAddress(EMAIL)))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        ForgotPasswordCommand command =
                new ForgotPasswordCommand(EMAIL);

        forgotPasswordService.forgotPassword(command);

        verify(passwordResetTokenRepository)
                .invalidateAllUnusedByUserId(
                        USER_ID,
                        NOW
                );

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );

        verify(passwordResetTokenRepository)
                .save(tokenCaptor.capture());

        PasswordResetToken savedToken =
                tokenCaptor.getValue();

        assertNull(savedToken.getId());

        assertEquals(
                USER_ID,
                savedToken.getUserId()
        );

        assertEquals(
                TOKEN_HASH,
                savedToken.getTokenHash()
        );

        assertEquals(
                NOW.plus(Duration.ofMinutes(30)),
                savedToken.getExpiresAt()
        );

        assertNull(savedToken.getUsedAt());

        verify(emailSender)
                .sendPasswordResetEmail(
                        EMAIL,
                        RAW_TOKEN
                );
    }

    @Test
    void shouldNotRevealWhenEmailDoesNotBelongToUser() {
        when(userRepository.findByEmail(new EmailAddress(EMAIL)))
                .thenReturn(Optional.empty());

        ForgotPasswordCommand command =
                new ForgotPasswordCommand(EMAIL);

        assertDoesNotThrow(
                () -> forgotPasswordService
                        .forgotPassword(command)
        );

        verifyNoInteractions(
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );
    }

    @Test
    void shouldInvalidatePreviousUnusedTokensBeforeIssuingNewToken() {
        User user = mock(User.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getEmail())
                .thenReturn(new EmailAddress(EMAIL));

        when(userRepository.findByEmail(new EmailAddress(EMAIL)))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        forgotPasswordService.forgotPassword(
                new ForgotPasswordCommand(EMAIL)
        );

        var inOrder = inOrder(
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );

        inOrder.verify(passwordResetTokenRepository)
                .invalidateAllUnusedByUserId(
                        USER_ID,
                        NOW
                );

        inOrder.verify(tokenGenerator)
                .generate();

        inOrder.verify(tokenHasher)
                .hash(RAW_TOKEN);

        inOrder.verify(passwordResetTokenRepository)
                .save(any(PasswordResetToken.class));

        inOrder.verify(emailSender)
                .sendPasswordResetEmail(
                        EMAIL,
                        RAW_TOKEN
                );
    }

    @Test
    void shouldStoreOnlyHashedTokenAndSendRawTokenByEmail() {
        User user = mock(User.class);

        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getEmail())
                .thenReturn(new EmailAddress(EMAIL));

        when(userRepository.findByEmail(new EmailAddress(EMAIL)))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        forgotPasswordService.forgotPassword(
                new ForgotPasswordCommand(EMAIL)
        );

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );

        verify(passwordResetTokenRepository)
                .save(tokenCaptor.capture());

        PasswordResetToken persistedToken =
                tokenCaptor.getValue();

        assertEquals(
                TOKEN_HASH,
                persistedToken.getTokenHash()
        );

        assertNotEquals(
                RAW_TOKEN,
                persistedToken.getTokenHash()
        );

        verify(emailSender)
                .sendPasswordResetEmail(
                        EMAIL,
                        RAW_TOKEN
                );
    }
}