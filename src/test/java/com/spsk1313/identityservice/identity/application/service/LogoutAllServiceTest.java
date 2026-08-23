package com.spsk1313.identityservice.identity.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutAllServiceTest {

    private static final Long USER_ID = 8L;

    @Mock
    private RevokeAllAuthSessionsService revokeAllAuthSessionsService;

    private LogoutAllService logoutAllService;

    @BeforeEach
    void setUp() {
        logoutAllService = new LogoutAllService(
                revokeAllAuthSessionsService
        );
    }

    @Test
    void shouldRevokeAllAuthSessionsForUser() {
        logoutAllService.logoutAll(USER_ID);

        verify(revokeAllAuthSessionsService)
                .revokeAll(USER_ID);
    }
}