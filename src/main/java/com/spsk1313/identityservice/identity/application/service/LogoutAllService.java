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

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;

    public LogoutAllService(AuthSessionRepository authSessionRepository, Clock clock) {
        this.authSessionRepository = authSessionRepository;
        this.clock = clock;
    }

    @Transactional
    public void logoutAll(Long userId) {
        if(userId == null) throw new IllegalArgumentException("User id cannot be null");

        List<AuthSession> sessions = authSessionRepository.findByUserId(userId);

        Instant now = clock.instant();

        for(var session: sessions) {
            if(session.isRevoked()) continue;
            session.revoke(now);
            authSessionRepository.save(session);
        }
    }
}
