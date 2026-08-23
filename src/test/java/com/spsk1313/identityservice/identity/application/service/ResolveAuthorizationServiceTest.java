package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.UserAuthorizationRepository;
import com.spsk1313.identityservice.identity.domain.authorization.PermissionName;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveAuthorizationServiceTest {

    @Mock
    private UserAuthorizationRepository userAuthorizationRepository;

    private ResolveAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new ResolveAuthorizationService(
                userAuthorizationRepository
        );
    }

    @Test
    void shouldResolveAuthorizationForUser() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        8L,
                        Set.of(RoleName.ADMIN),
                        Set.of(
                                PermissionName.USER_READ,
                                PermissionName.USER_DISABLE,
                                PermissionName.SESSION_REVOKE,
                                PermissionName.ROLE_ASSIGN
                        )
                );

        when(userAuthorizationRepository.findByUserId(8L))
                .thenReturn(authorization);

        UserAuthorization result =
                service.resolve(8L);

        assertSame(
                authorization,
                result
        );

        verify(userAuthorizationRepository)
                .findByUserId(8L);
    }
}