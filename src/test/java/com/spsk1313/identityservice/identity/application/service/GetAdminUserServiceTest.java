package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.result.AdminUserResult;
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
class GetAdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private GetAdminUserService service;

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "sahil@example.com";
    private static final String PASSWORD_HASH = "stored-password-hash";

    @BeforeEach
    void setUp() {
        service = new GetAdminUserService(
                userRepository
        );
    }

    @Test
    void shouldReturnUserWhenUserExists() {
        User user = User.reconstitute(
                USER_ID,
                new EmailAddress(EMAIL),
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        AdminUserResult result =
                service.getById(USER_ID);

        assertEquals(USER_ID, result.id());
        assertEquals(EMAIL, result.email());
        assertTrue(result.emailVerified());
        assertEquals(
                AccountStatus.ACTIVE,
                result.accountStatus()
        );

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.getById(USER_ID)
        );

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectZeroUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(0L)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectNegativeUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(-1L)
        );

        verifyNoInteractions(userRepository);
    }
}