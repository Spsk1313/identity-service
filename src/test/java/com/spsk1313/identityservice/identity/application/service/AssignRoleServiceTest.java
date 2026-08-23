package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    private AssignRoleService assignRoleService;

    @BeforeEach
    void setUp() {
        assignRoleService =
                new AssignRoleService(userRoleRepository);
    }

    @Test
    void shouldAssignRoleToUser() {
        assignRoleService.assign(
                8L,
                RoleName.USER
        );

        ArgumentCaptor<UserRole> captor =
                ArgumentCaptor.forClass(UserRole.class);

        verify(userRoleRepository)
                .save(captor.capture());

        UserRole savedUserRole = captor.getValue();

        assertEquals(
                8L,
                savedUserRole.getUserId()
        );

        assertEquals(
                RoleName.USER,
                savedUserRole.getRole()
        );
    }

    @Test
    void shouldRejectNullUserId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> assignRoleService.assign(
                                null,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void shouldRejectNonPositiveUserId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> assignRoleService.assign(
                                0L,
                                RoleName.USER
                        )
                );

        assertEquals(
                "User id must be positive",
                exception.getMessage()
        );

        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void shouldRejectNullRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> assignRoleService.assign(
                                8L,
                                null
                        )
                );

        assertEquals(
                "Role cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(userRoleRepository);
    }
}