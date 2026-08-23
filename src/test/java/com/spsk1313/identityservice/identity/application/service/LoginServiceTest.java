package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.LoginCommand;
import com.spsk1313.identityservice.identity.application.exception.AccountDisabledException;
import com.spsk1313.identityservice.identity.application.exception.EmailNotVerifiedException;
import com.spsk1313.identityservice.identity.application.exception.InvalidCredentialsException;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String EMAIL_STR = "sahil@example.com";
    private static final EmailAddress EMAIL = new EmailAddress(EMAIL_STR);

    private static final String PASSWORD =
            "correct-horse-battery-staple";

    private static final String PASSWORD_HASH =
            "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                userRepository,
                passwordHasher
        );
    }

    @Test
    void shouldLoginWhenCredentialsAreValid() {
        User user = User.reconstitute(
                1L,
                EMAIL,
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        LoginResult result = loginService.login(
                new LoginCommand(EMAIL_STR, PASSWORD)
        );

        assertEquals(1L, result.userId());
        assertEquals(EMAIL_STR, result.email());

        verify(passwordHasher)
                .matches(PASSWORD, PASSWORD_HASH);
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        new LoginCommand(EMAIL_STR, PASSWORD)
                )
        );

        verifyNoInteractions(passwordHasher);
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        User user = User.reconstitute(
                1L,
                EMAIL,
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        new LoginCommand(EMAIL_STR, PASSWORD)
                )
        );
    }

    @Test
    void shouldRejectLoginWhenEmailIsNotVerified() {
        User user = User.register(
                EMAIL,
                PASSWORD_HASH
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        assertThrows(
                EmailNotVerifiedException.class,
                () -> loginService.login(
                        new LoginCommand(EMAIL_STR, PASSWORD)
                )
        );
    }

    @Test
    void shouldRejectLoginWhenAccountIsDisabled() {
        User user = User.register(
                EMAIL,
                PASSWORD_HASH
        );

        user.verifyEmail();
        user.disable();

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        assertThrows(
                AccountDisabledException.class,
                () -> loginService.login(
                        new LoginCommand(EMAIL_STR, PASSWORD)
                )
        );

        verify(passwordHasher)
                .matches(PASSWORD, PASSWORD_HASH);
    }
}