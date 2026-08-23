package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class RevokeAuthSessionService {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;

    public RevokeAuthSessionService(AuthSessionRepository authSessionRepository, Clock clock) {
        this.authSessionRepository = authSessionRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revoke(AuthSession session) {
        session.revoke(clock.instant());
        authSessionRepository.save(session);
    }
}
