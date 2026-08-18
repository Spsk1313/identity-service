package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.RawTokenGenerator;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.VerificationEmailSender;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
class IssueEmailVerificationServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "sahil@example.com";

    private static final String RAW_TOKEN =
            "raw-verification-token";

    private static final String TOKEN_HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final Instant NOW =
            Instant.parse("2026-08-18T12:00:00Z");

    private static final String BASE_URL =
            "https://identity.example.com";

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private RawTokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private VerificationEmailSender emailSender;

    private Clock clock;

    private IssueEmailVerificationService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new IssueEmailVerificationService(
                tokenRepository,
                tokenGenerator,
                tokenHasher,
                emailSender,
                clock,
                BASE_URL
        );
    }

    @Test
    void shouldIssueVerificationTokenAndSendEmail() {
        when(tokenRepository.findOutstandingByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(USER_ID, EMAIL);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();

        assertEquals(USER_ID, savedToken.getUserId());
        assertEquals(TOKEN_HASH, savedToken.getTokenHash());
        assertEquals(
                NOW.plus(Duration.ofHours(24)),
                savedToken.getExpiresAt()
        );
        assertFalse(savedToken.isUsed());
        assertFalse(savedToken.isInvalidated());

        String expectedLink =
                BASE_URL + "/api/auth/verify-email?token=" + RAW_TOKEN;

        verify(emailSender)
                .sendVerificationEmail(EMAIL, expectedLink);
    }

    @Test
    void shouldInvalidateOutstandingTokenBeforeIssuingNewOne() {
        EmailVerificationToken existingToken =
                EmailVerificationToken.issue(
                        USER_ID,
                        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                        NOW.plus(Duration.ofHours(1))
                );

        when(tokenRepository.findOutstandingByUserId(USER_ID))
                .thenReturn(Optional.of(existingToken));

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(USER_ID, EMAIL);

        assertTrue(existingToken.isInvalidated());
        assertEquals(NOW, existingToken.getInvalidatedAt());

        InOrder inOrder = inOrder(tokenRepository);

        inOrder.verify(tokenRepository)
                .findOutstandingByUserId(USER_ID);

        inOrder.verify(tokenRepository)
                .save(existingToken);

        inOrder.verify(tokenRepository)
                .save(argThat(token ->
                        token != existingToken
                                && TOKEN_HASH.equals(token.getTokenHash())
                ));
    }

    @Test
    void shouldNeverPersistRawVerificationToken() {
        when(tokenRepository.findOutstandingByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(USER_ID, EMAIL);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken persistedToken =
                tokenCaptor.getValue();

        assertEquals(TOKEN_HASH, persistedToken.getTokenHash());
        assertNotEquals(RAW_TOKEN, persistedToken.getTokenHash());
    }

    @Test
    void shouldUseConfiguredBaseUrlInVerificationLink() {
        when(tokenRepository.findOutstandingByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(USER_ID, EMAIL);

        verify(emailSender).sendVerificationEmail(
                EMAIL,
                BASE_URL + "/api/auth/verify-email?token=" + RAW_TOKEN
        );
    }
}