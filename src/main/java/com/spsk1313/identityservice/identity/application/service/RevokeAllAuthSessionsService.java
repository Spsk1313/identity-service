package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class RevokeAllAuthSessionsService {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;

    public RevokeAllAuthSessionsService(
            AuthSessionRepository authSessionRepository,
            Clock clock
    ) {
        this.authSessionRepository = authSessionRepository;
        this.clock = clock;
    }

    @Transactional
    public void revokeAll(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id cannot be null"
            );
        }

        Instant now = clock.instant();

        for (AuthSession session :
                authSessionRepository.findByUserId(userId)) {

            if (session.isRevoked()) {
                continue;
            }

            session.revoke(now);
            authSessionRepository.save(session);
        }
    }
}
