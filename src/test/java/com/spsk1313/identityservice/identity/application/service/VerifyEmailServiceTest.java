package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.VerificationTokenNotFoundException;
import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import com.spsk1313.identityservice.identity.domain.verification.VerificationTokenNotUsableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenHasher tokenHasher;

    private VerifyEmailService service;

    private static final Long USER_ID = 1L;

    private static final String EMAIL =
            "sahil@example.com";

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    private static final String RAW_TOKEN =
            "raw-verification-token";

    private static final String TOKEN_HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final Instant NOW =
            Instant.parse("2026-08-18T12:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new VerifyEmailService(
                tokenRepository,
                userRepository,
                tokenHasher,
                clock
        );
    }

    @Test
    void shouldVerifyEmailWithValidToken() {
        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        USER_ID,
                        TOKEN_HASH,
                        NOW.plus(Duration.ofHours(24))
                );

        User user =
                User.register(
                        new EmailAddress(EMAIL),
                        PASSWORD_HASH
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        service.verify(RAW_TOKEN);

        assertTrue(token.isUsed());
        assertEquals(NOW, token.getUsedAt());

        assertTrue(user.isEmailVerified());

        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUnknownVerificationToken() {
        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        assertThrows(
                VerificationTokenNotFoundException.class,
                () -> service.verify(RAW_TOKEN)
        );

        verifyNoInteractions(userRepository);

        verify(tokenRepository, never())
                .save(any(EmailVerificationToken.class));
    }

    @Test
    void shouldRejectExpiredVerificationToken() {
        EmailVerificationToken expiredToken =
                EmailVerificationToken.issue(
                        USER_ID,
                        TOKEN_HASH,
                        NOW
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                VerificationTokenNotUsableException.class,
                () -> service.verify(RAW_TOKEN)
        );

        verifyNoInteractions(userRepository);

        verify(tokenRepository, never())
                .save(any(EmailVerificationToken.class));
    }

    @Test
    void shouldRejectInvalidatedVerificationToken() {
        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        USER_ID,
                        TOKEN_HASH,
                        NOW.plus(Duration.ofHours(24))
                );

        token.invalidate(
                NOW.minus(Duration.ofMinutes(1))
        );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(token));

        assertThrows(
                VerificationTokenNotUsableException.class,
                () -> service.verify(RAW_TOKEN)
        );

        verifyNoInteractions(userRepository);

        verify(tokenRepository, never())
                .save(any(EmailVerificationToken.class));
    }
}