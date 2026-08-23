package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.LoginCommand;
import com.spsk1313.identityservice.identity.application.exception.AccountDisabledException;
import com.spsk1313.identityservice.identity.application.exception.EmailNotVerifiedException;
import com.spsk1313.identityservice.identity.application.exception.InvalidCredentialsException;
import com.spsk1313.identityservice.identity.application.port.in.AuthorizationResolver;
import com.spsk1313.identityservice.identity.application.port.out.*;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RawTokenGenerator rawTokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private AuthorizationResolver authorizationResolver;

    private LoginService loginService;

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 10L;

    private static final String EMAIL = "sahil@example.com";
    private static final String PASSWORD =
            "correct-horse-battery-staple";
    private static final String PASSWORD_HASH =
            "stored-password-hash";

    private static final String USER_AGENT =
            "Mozilla/5.0 Test Browser";

    private static final String RAW_REFRESH_TOKEN =
            "raw-refresh-token";

    private static final String REFRESH_TOKEN_HASH =
            "hashed-refresh-token";

    private static final String ACCESS_TOKEN =
            "signed-access-jwt";

    private static final Instant NOW =
            Instant.parse("2026-08-23T12:00:00Z");

    private static final Instant SESSION_EXPIRES_AT =
            NOW.plus(Duration.ofDays(30));

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        loginService = new LoginService(
                userRepository,
                passwordHasher,
                authSessionRepository,
                refreshTokenRepository,
                rawTokenGenerator,
                tokenHasher,
                accessTokenIssuer,
                clock,
                authorizationResolver
        );
    }

    @Test
    void shouldLoginAndIssueCredentialsWhenAuthenticationIsValid() {
        User user = verifiedActiveUser();

        UserAuthorization authorization =
                userAuthorization();

        AuthSession persistedSession =
                AuthSession.reconstitute(
                        SESSION_ID,
                        USER_ID,
                        SESSION_EXPIRES_AT,
                        null,
                        null,
                        USER_AGENT
                );

        when(userRepository.findByEmail(
                new EmailAddress(EMAIL)
        )).thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                PASSWORD,
                PASSWORD_HASH
        )).thenReturn(true);

        when(authSessionRepository.save(
                any(AuthSession.class)
        )).thenReturn(persistedSession);

        when(rawTokenGenerator.generate())
                .thenReturn(RAW_REFRESH_TOKEN);

        when(tokenHasher.hash(RAW_REFRESH_TOKEN))
                .thenReturn(REFRESH_TOKEN_HASH);

        when(authorizationResolver.resolve(USER_ID))
                .thenReturn(authorization);

        when(accessTokenIssuer.issue(
                USER_ID,
                EMAIL,
                authorization
        )).thenReturn(ACCESS_TOKEN);

        LoginCommand command =
                new LoginCommand(
                        EMAIL,
                        PASSWORD,
                        USER_AGENT
                );

        LoginResult result =
                loginService.login(command);

        assertEquals(USER_ID, result.userId());
        assertEquals(EMAIL, result.email());
        assertEquals(
                ACCESS_TOKEN,
                result.accessToken()
        );
        assertEquals(
                RAW_REFRESH_TOKEN,
                result.refreshToken()
        );

        ArgumentCaptor<AuthSession> sessionCaptor =
                ArgumentCaptor.forClass(
                        AuthSession.class
                );

        verify(authSessionRepository)
                .save(sessionCaptor.capture());

        AuthSession session =
                sessionCaptor.getValue();

        assertEquals(
                USER_ID,
                session.getUserId()
        );

        assertEquals(
                SESSION_EXPIRES_AT,
                session.getExpiresAt()
        );

        assertEquals(
                USER_AGENT,
                session.getUserAgent()
        );

        assertNull(session.getRevokedAt());
        assertNull(session.getLastUsedAt());

        verify(rawTokenGenerator).generate();

        verify(tokenHasher)
                .hash(RAW_REFRESH_TOKEN);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(
                        RefreshToken.class
                );

        verify(refreshTokenRepository)
                .save(refreshTokenCaptor.capture());

        RefreshToken refreshToken =
                refreshTokenCaptor.getValue();

        assertEquals(
                SESSION_ID,
                refreshToken.getSessionId()
        );

        assertEquals(
                REFRESH_TOKEN_HASH,
                refreshToken.getTokenHash()
        );

        assertEquals(
                SESSION_EXPIRES_AT,
                refreshToken.getExpiresAt()
        );

        assertNotEquals(
                RAW_REFRESH_TOKEN,
                refreshToken.getTokenHash()
        );

        verify(authorizationResolver)
                .resolve(USER_ID);

        verify(accessTokenIssuer)
                .issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );
    }

    @Test
    void shouldRejectLoginWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(
                new EmailAddress(EMAIL)
        )).thenReturn(Optional.empty());

        LoginCommand command =
                new LoginCommand(
                        EMAIL,
                        PASSWORD,
                        USER_AGENT
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(command)
        );

        verifyNoInteractions(passwordHasher);

        verifyNoAuthenticationArtifactsCreated();
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        User user = verifiedActiveUser();

        when(userRepository.findByEmail(
                new EmailAddress(EMAIL)
        )).thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                PASSWORD,
                PASSWORD_HASH
        )).thenReturn(false);

        LoginCommand command =
                new LoginCommand(
                        EMAIL,
                        PASSWORD,
                        USER_AGENT
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(command)
        );

        verifyNoAuthenticationArtifactsCreated();
    }

    @Test
    void shouldRejectLoginWhenEmailIsNotVerified() {
        User user =
                User.reconstitute(
                        USER_ID,
                        new EmailAddress(EMAIL),
                        PASSWORD_HASH,
                        false,
                        AccountStatus.ACTIVE
                );

        when(userRepository.findByEmail(
                new EmailAddress(EMAIL)
        )).thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                PASSWORD,
                PASSWORD_HASH
        )).thenReturn(true);

        LoginCommand command =
                new LoginCommand(
                        EMAIL,
                        PASSWORD,
                        USER_AGENT
                );

        assertThrows(
                EmailNotVerifiedException.class,
                () -> loginService.login(command)
        );

        verifyNoAuthenticationArtifactsCreated();
    }

    @Test
    void shouldRejectLoginWhenAccountIsDisabled() {
        User user =
                User.reconstitute(
                        USER_ID,
                        new EmailAddress(EMAIL),
                        PASSWORD_HASH,
                        true,
                        AccountStatus.DISABLED
                );

        when(userRepository.findByEmail(
                new EmailAddress(EMAIL)
        )).thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                PASSWORD,
                PASSWORD_HASH
        )).thenReturn(true);

        LoginCommand command =
                new LoginCommand(
                        EMAIL,
                        PASSWORD,
                        USER_AGENT
                );

        assertThrows(
                AccountDisabledException.class,
                () -> loginService.login(command)
        );

        verifyNoAuthenticationArtifactsCreated();
    }

    private User verifiedActiveUser() {
        return User.reconstitute(
                USER_ID,
                new EmailAddress(EMAIL),
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );
    }

    private UserAuthorization userAuthorization() {
        return UserAuthorization.of(
                USER_ID,
                Set.of(RoleName.USER),
                Set.of()
        );
    }

    private void verifyNoAuthenticationArtifactsCreated() {
        verifyNoInteractions(
                authSessionRepository,
                refreshTokenRepository,
                rawTokenGenerator,
                tokenHasher,
                accessTokenIssuer,
                authorizationResolver
        );
    }
}