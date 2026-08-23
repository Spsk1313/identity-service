package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.RefreshAccessTokenCommand;
import com.spsk1313.identityservice.identity.application.exception.InvalidRefreshTokenException;
import com.spsk1313.identityservice.identity.application.port.out.*;
import com.spsk1313.identityservice.identity.application.result.RefreshAccessTokenResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.spsk1313.identityservice.identity.application.port.in.AuthorizationResolver;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;

import java.util.Set;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenServiceTest {

    @Mock
    private RevokeAuthSessionService revokeAuthSessionService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private RawTokenGenerator tokenGenerator;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private AuthorizationResolver authorizationResolver;

    private RefreshAccessTokenService service;

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 10L;
    private static final Long CURRENT_REFRESH_TOKEN_ID = 20L;

    private static final String EMAIL = "sahil@example.com";
    private static final String PASSWORD_HASH = "stored-password-hash";

    private static final String RAW_REFRESH_TOKEN = "raw-refresh-token-r1";
    private static final String CURRENT_TOKEN_HASH = "hash-r1";

    private static final String NEW_RAW_REFRESH_TOKEN = "raw-refresh-token-r2";
    private static final String NEW_TOKEN_HASH = "hash-r2";

    private static final String NEW_ACCESS_TOKEN = "new-access-token";

    private static final String USER_AGENT = "Test Browser";

    private static final Instant NOW =
            Instant.parse("2026-08-23T12:00:00Z");

    private static final Instant SESSION_EXPIRES_AT =
            NOW.plus(Duration.ofDays(20));

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new RefreshAccessTokenService(
                revokeAuthSessionService,
                refreshTokenRepository,
                authSessionRepository,
                userRepository,
                tokenHasher,
                tokenGenerator,
                accessTokenIssuer,
                clock,
                authorizationResolver
        );
    }

    @Test
    void shouldRotateRefreshTokenAndIssueNewAccessToken() {
        RefreshToken currentRefreshToken = activeRefreshToken();
        AuthSession session = activeSession();
        User user = activeUser();
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(RoleName.USER),
                        Set.of()
                );

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(NEW_RAW_REFRESH_TOKEN);

        when(tokenHasher.hash(NEW_RAW_REFRESH_TOKEN))
                .thenReturn(NEW_TOKEN_HASH);

        when(authorizationResolver.resolve(USER_ID))
                .thenReturn(authorization);

        when(accessTokenIssuer.issue(
                USER_ID,
                EMAIL,
                authorization
        )).thenReturn(NEW_ACCESS_TOKEN);

        RefreshAccessTokenResult result = service.refresh(
                new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
        );

        // Returned credentials
        assertEquals(NEW_ACCESS_TOKEN, result.accessToken());
        assertEquals(NEW_RAW_REFRESH_TOKEN, result.refreshToken());

        // Capture BOTH refresh-token saves:
        // 1. consumed R1
        // 2. newly issued R2
        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository, times(2))
                .save(tokenCaptor.capture());

        var savedTokens = tokenCaptor.getAllValues();

        RefreshToken savedCurrentToken = savedTokens.get(0);
        RefreshToken newRefreshToken = savedTokens.get(1);

        // R1 was consumed
        assertSame(currentRefreshToken, savedCurrentToken);
        assertTrue(savedCurrentToken.isUsed());
        assertEquals(NOW, savedCurrentToken.getUsedAt());

        // Session activity was updated
        assertEquals(NOW, session.getLastUsedAt());
        verify(authSessionRepository).save(session);

        // R2 belongs to the same session
        assertEquals(
                SESSION_ID,
                newRefreshToken.getSessionId()
        );

        // Only H(R2) was persisted
        assertEquals(
                NEW_TOKEN_HASH,
                newRefreshToken.getTokenHash()
        );

        assertNotEquals(
                NEW_RAW_REFRESH_TOKEN,
                newRefreshToken.getTokenHash()
        );

        // Rotation does NOT extend the session lifetime
        assertEquals(
                SESSION_EXPIRES_AT,
                newRefreshToken.getExpiresAt()
        );

        // R2 starts unused
        assertFalse(newRefreshToken.isUsed());
        assertNull(newRefreshToken.getUsedAt());

        verify(tokenHasher).hash(RAW_REFRESH_TOKEN);
        verify(tokenHasher).hash(NEW_RAW_REFRESH_TOKEN);

        verify(tokenGenerator).generate();
        verify(authorizationResolver)
                .resolve(USER_ID);

        verify(accessTokenIssuer)
                .issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        verifyNoInteractions(revokeAuthSessionService);
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verify(refreshTokenRepository)
                .findByTokenHash(CURRENT_TOKEN_HASH);

        verifyNoInteractions(
                authSessionRepository,
                userRepository,
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken expiredToken = RefreshToken.reconstitute(
                CURRENT_REFRESH_TOKEN_ID,
                SESSION_ID,
                CURRENT_TOKEN_HASH,
                NOW.minusSeconds(1),
                null
        );

        AuthSession session = activeSession();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(expiredToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verifyNoInteractions(
                userRepository,
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRevokeSessionWhenUsedRefreshTokenIsReplayed() {
        RefreshToken usedToken = RefreshToken.reconstitute(
                CURRENT_REFRESH_TOKEN_ID,
                SESSION_ID,
                CURRENT_TOKEN_HASH,
                SESSION_EXPIRES_AT,
                NOW.minusSeconds(60)
        );

        AuthSession session = activeSession();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(usedToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verify(revokeAuthSessionService).revoke(session);

        verifyNoInteractions(
                userRepository,
                tokenGenerator,
                accessTokenIssuer
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRejectRefreshWhenSessionDoesNotExist() {
        RefreshToken currentRefreshToken = activeRefreshToken();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        assertFalse(currentRefreshToken.isUsed());

        verifyNoInteractions(
                userRepository,
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRejectRefreshWhenSessionIsRevoked() {
        RefreshToken currentRefreshToken = activeRefreshToken();

        AuthSession revokedSession = AuthSession.reconstitute(
                SESSION_ID,
                USER_ID,
                SESSION_EXPIRES_AT,
                NOW.minusSeconds(60),
                null,
                USER_AGENT
        );

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(revokedSession));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verifyNoInteractions(
                userRepository,
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRejectRefreshWhenSessionIsExpired() {
        RefreshToken currentRefreshToken = RefreshToken.reconstitute(
                CURRENT_REFRESH_TOKEN_ID,
                SESSION_ID,
                CURRENT_TOKEN_HASH,
                NOW.plus(Duration.ofDays(1)),
                null
        );

        AuthSession expiredSession = AuthSession.reconstitute(
                SESSION_ID,
                USER_ID,
                NOW.minusSeconds(1),
                null,
                null,
                USER_AGENT
        );

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(expiredSession));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verifyNoInteractions(
                userRepository,
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRejectRefreshWhenUserDoesNotExist() {
        RefreshToken currentRefreshToken = activeRefreshToken();
        AuthSession session = activeSession();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verifyNoInteractions(
                tokenGenerator,
                accessTokenIssuer,
                revokeAuthSessionService
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    @Test
    void shouldRevokeSessionWhenUserIsDisabled() {
        RefreshToken currentRefreshToken = activeRefreshToken();
        AuthSession session = activeSession();
        User disabledUser = disabledUser();

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(CURRENT_TOKEN_HASH);

        when(refreshTokenRepository.findByTokenHash(CURRENT_TOKEN_HASH))
                .thenReturn(Optional.of(currentRefreshToken));

        when(authSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(disabledUser));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.refresh(
                        new RefreshAccessTokenCommand(RAW_REFRESH_TOKEN)
                )
        );

        verify(revokeAuthSessionService).revoke(session);

        verifyNoInteractions(
                tokenGenerator,
                accessTokenIssuer
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(authSessionRepository, never())
                .save(any(AuthSession.class));
    }

    private RefreshToken activeRefreshToken() {
        return RefreshToken.reconstitute(
                CURRENT_REFRESH_TOKEN_ID,
                SESSION_ID,
                CURRENT_TOKEN_HASH,
                SESSION_EXPIRES_AT,
                null
        );
    }

    private AuthSession activeSession() {
        return AuthSession.reconstitute(
                SESSION_ID,
                USER_ID,
                SESSION_EXPIRES_AT,
                null,
                null,
                USER_AGENT
        );
    }

    private User activeUser() {
        return User.reconstitute(
                USER_ID,
                new EmailAddress(EMAIL),
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );
    }

    private User disabledUser() {
        return User.reconstitute(
                USER_ID,
                new EmailAddress(EMAIL),
                PASSWORD_HASH,
                true,
                AccountStatus.DISABLED
        );
    }
}