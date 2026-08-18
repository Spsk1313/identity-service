package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.RegisterUserCommand;
import com.spsk1313.identityservice.identity.application.exception.DuplicateEmailException;
import com.spsk1313.identityservice.identity.application.exception.InvalidPasswordException;
import com.spsk1313.identityservice.identity.application.policy.PasswordPolicy;
import com.spsk1313.identityservice.identity.application.port.in.EmailVerificationIssuer;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterUserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    EmailVerificationIssuer emailVerificationIssuer;

    private RegisterUserService registerUserService;

    private static final String EMAIL_STR = "sahil@example.com";
    private static final EmailAddress EMAIL = new EmailAddress(EMAIL_STR);
    private static final String PASSWORD = "correct-horse-battery-staple";
    private static final String PASSWORD_HASH = "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    @BeforeEach
    void setUp() {
        PasswordPolicy passwordPolicy = new PasswordPolicy();
        registerUserService = new RegisterUserService(userRepository, passwordHasher, passwordPolicy, emailVerificationIssuer);
    }

    @Test
    void shouldRegisterUserWhenCredentialsAreValid() {
        RegisterUserCommand command = new RegisterUserCommand(EMAIL_STR, PASSWORD);

        User persistedUser = User.reconstitute(
                1L,
                EMAIL,
                PASSWORD_HASH,
                false,
                AccountStatus.ACTIVE
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        when(passwordHasher.hash(PASSWORD)).thenReturn(PASSWORD_HASH);

        when(userRepository.save(any(User.class))).thenReturn(persistedUser);

        RegisterUserResult result = registerUserService.register(command);

        assertEquals(1L, result.id());
        assertEquals(EMAIL_STR, result.email());
        assertFalse(result.emailVerified());
        assertEquals(AccountStatus.ACTIVE, result.accountStatus());

        verify(userRepository).save(captor.capture());

        User capturedUser = captor.getValue();
        assertEquals(PASSWORD_HASH, capturedUser.getPasswordHash());

        verify(emailVerificationIssuer).issue(persistedUser.getId(), persistedUser.getEmail().value());
    }


    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand(EMAIL_STR, PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> registerUserService.register(command));

        verifyNoInteractions(passwordHasher);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailVerificationIssuer);
    }

    @Test
    void shouldRejectRegistrationWhenPasswordIsInvalid() {
        RegisterUserCommand command = new RegisterUserCommand(EMAIL_STR, "password");

        assertThrows(InvalidPasswordException.class, () -> registerUserService.register(command));

        verifyNoInteractions(passwordHasher);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(emailVerificationIssuer);
    }

}
