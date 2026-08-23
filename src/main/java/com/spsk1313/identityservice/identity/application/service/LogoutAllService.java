package com.spsk1313.identityservice.identity.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
