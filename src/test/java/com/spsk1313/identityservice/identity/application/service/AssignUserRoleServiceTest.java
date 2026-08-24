package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignUserRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    private AssignUserRoleService service;

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD_HASH = "stored-password-hash";

    @BeforeEach
    void setUp() {
        service = new AssignUserRoleService(
                userRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldAssignRoleWhenUserExistsAndRoleIsNotAlreadyAssigned() {
        User user = existingUser();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userRoleRepository.existsByUserIdAndRole(
                USER_ID,
                RoleName.MODERATOR
        )).thenReturn(false);

        service.assign(
                USER_ID,
                RoleName.MODERATOR
        );

        ArgumentCaptor<UserRole> captor =
                ArgumentCaptor.forClass(UserRole.class);

        verify(userRoleRepository)
                .save(captor.capture());

        UserRole savedUserRole = captor.getValue();

        assertEquals(
                USER_ID,
                savedUserRole.getUserId()
        );

        assertEquals(
                RoleName.MODERATOR,
                savedUserRole.getRole()
        );

        verify(userRepository)
                .findById(USER_ID);

        verify(userRoleRepository)
                .existsByUserIdAndRole(
                        USER_ID,
                        RoleName.MODERATOR
                );
    }

    @Test
    void shouldDoNothingWhenRoleIsAlreadyAssigned() {
        User user = existingUser();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userRoleRepository.existsByUserIdAndRole(
                USER_ID,
                RoleName.MODERATOR
        )).thenReturn(true);

        service.assign(
                USER_ID,
                RoleName.MODERATOR
        );

        verify(userRepository)
                .findById(USER_ID);

        verify(userRoleRepository)
                .existsByUserIdAndRole(
                        USER_ID,
                        RoleName.MODERATOR
                );

        verify(userRoleRepository, never())
                .save(any(UserRole.class));
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.assign(
                        USER_ID,
                        RoleName.MODERATOR
                )
        );

        verify(userRepository)
                .findById(USER_ID);

        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.assign(
                        null,
                        RoleName.MODERATOR
                )
        );

        verifyNoInteractions(
                userRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldRejectZeroUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.assign(
                        0L,
                        RoleName.MODERATOR
                )
        );

        verifyNoInteractions(
                userRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldRejectNegativeUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.assign(
                        -1L,
                        RoleName.MODERATOR
                )
        );

        verifyNoInteractions(
                userRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldRejectNullRole() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.assign(
                        USER_ID,
                        null
                )
        );

        verifyNoInteractions(
                userRepository,
                userRoleRepository
        );
    }

    private User existingUser() {
        return User.reconstitute(
                USER_ID,
                new EmailAddress(EMAIL),
                PASSWORD_HASH,
                true,
                AccountStatus.ACTIVE
        );
    }
}