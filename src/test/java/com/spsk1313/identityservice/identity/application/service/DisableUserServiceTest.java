package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.in.UserSessionRevoker;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisableUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRevoker userSessionRevoker;

    private DisableUserService service;

    private static final Long USER_ID = 42L;

    @BeforeEach
    void setUp() {
        service = new DisableUserService(
                userRepository,
                userSessionRevoker
        );
    }

    @Test
    void shouldDisableUserAndRevokeAllSessions() {
        User user = activeUser();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        service.disable(USER_ID);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(
                AccountStatus.DISABLED,
                savedUser.getAccountStatus()
        );

        verify(userSessionRevoker)
                .revokeAll(USER_ID);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.disable(USER_ID)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                userSessionRevoker
        );
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.disable(null)
        );

        verifyNoInteractions(
                userRepository,
                userSessionRevoker
        );
    }

    @Test
    void shouldRejectZeroUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.disable(0L)
        );

        verifyNoInteractions(
                userRepository,
                userSessionRevoker
        );
    }

    @Test
    void shouldRejectNegativeUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.disable(-1L)
        );

        verifyNoInteractions(
                userRepository,
                userSessionRevoker
        );
    }

    private User activeUser() {
        return User.reconstitute(
                USER_ID,
                new EmailAddress(
                        "user@example.com"
                ),
                "stored-password-hash",
                true,
                AccountStatus.ACTIVE
        );
    }
}