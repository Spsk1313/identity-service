package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class LogoutAllService {

    private final RevokeAllAuthSessionsService revokeAllAuthSessionsService;

    public LogoutAllService(RevokeAllAuthSessionsService revokeAllAuthSessionsService) {
        this.revokeAllAuthSessionsService = revokeAllAuthSessionsService;
    }

    @Transactional
    public void logoutAll(Long userId) {
        revokeAllAuthSessionsService.revokeAll(userId);
    }
}
