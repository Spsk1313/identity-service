package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.application.port.out.RefreshTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import com.spsk1313.identityservice.identity.domain.auth.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenHasher tokenHasher;

    private LogoutService logoutService;

    private static final Instant NOW =
            Instant.parse("2026-08-23T17:00:00Z");

    private static final Instant EXPIRES_AT =
            NOW.plusSeconds(3600);

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 10L;
    private static final Long REFRESH_TOKEN_ID = 20L;

    private static final String RAW_REFRESH_TOKEN =
            "raw-refresh-token";

    private static final String TOKEN_HASH =
            "hashed-refresh-token";

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        logoutService = new LogoutService(
                refreshTokenRepository,
                authSessionRepository,
                tokenHasher,
                clock
        );
    }

    @Test
    void shouldRevokeAndPersistSessionWhenRefreshTokenIsValid() {
        RefreshToken refreshToken = activeRefreshToken();
        AuthSession session = activeSession();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(refreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        logoutService.logout(RAW_REFRESH_TOKEN);

        assertTrue(session.isRevoked());
        assertEquals(NOW, session.getRevokedAt());

        verify(tokenHasher).hash(RAW_REFRESH_TOKEN);

        verify(refreshTokenRepository)
                .findByTokenHash(TOKEN_HASH);

        verify(authSessionRepository)
                .findById(SESSION_ID);

        verify(authSessionRepository)
                .save(session);
    }

    @Test
    void shouldDoNothingWhenRefreshTokenIsNull() {
        logoutService.logout(null);

        verifyNoInteractions(
                tokenHasher,
                refreshTokenRepository,
                authSessionRepository
        );
    }

    @Test
    void shouldDoNothingWhenRefreshTokenIsBlank() {
        logoutService.logout("   ");

        verifyNoInteractions(
                tokenHasher,
                refreshTokenRepository,
                authSessionRepository
        );
    }

    @Test
    void shouldDoNothingWhenRefreshTokenDoesNotExist() {
        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        logoutService.logout(RAW_REFRESH_TOKEN);

        verify(tokenHasher)
                .hash(RAW_REFRESH_TOKEN);

        verify(refreshTokenRepository)
                .findByTokenHash(TOKEN_HASH);

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void shouldDoNothingWhenSessionDoesNotExist() {
        RefreshToken refreshToken = activeRefreshToken();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(refreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.empty());

        logoutService.logout(RAW_REFRESH_TOKEN);

        verify(authSessionRepository)
                .findById(SESSION_ID);

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldDoNothingWhenSessionIsAlreadyRevoked() {
        RefreshToken refreshToken = activeRefreshToken();

        Instant originallyRevokedAt =
                NOW.minusSeconds(300);

        AuthSession revokedSession =
                AuthSession.reconstitute(
                        SESSION_ID,
                        USER_ID,
                        EXPIRES_AT,
                        originallyRevokedAt,
                        null,
                        "Test Browser"
                );

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(refreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(revokedSession));

        logoutService.logout(RAW_REFRESH_TOKEN);

        assertTrue(revokedSession.isRevoked());

        assertEquals(
                originallyRevokedAt,
                revokedSession.getRevokedAt()
        );

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    private RefreshToken activeRefreshToken() {
        return RefreshToken.reconstitute(
                REFRESH_TOKEN_ID,
                SESSION_ID,
                TOKEN_HASH,
                EXPIRES_AT,
                null
        );
    }

    private AuthSession activeSession() {
        return AuthSession.reconstitute(
                SESSION_ID,
                USER_ID,
                EXPIRES_AT,
                null,
                null,
                "Test Browser"
        );
    }
}