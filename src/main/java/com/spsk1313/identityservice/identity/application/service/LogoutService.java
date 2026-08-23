package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.port.out.AuthSessionRepository;
import com.spsk1313.identityservice.identity.application.port.out.RefreshTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Service
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthSessionRepository authSessionRepository;
    private final TokenHasher tokenHasher;
    private final Clock clock;

    public LogoutService(RefreshTokenRepository refreshTokenRepository, AuthSessionRepository authSessionRepository, TokenHasher tokenHasher, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authSessionRepository = authSessionRepository;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = tokenHasher.hash(rawRefreshToken);

        Optional<RefreshToken> refreshToken =
                refreshTokenRepository.findByTokenHash(tokenHash);

        if (refreshToken.isEmpty()) {
            return;
        }

        Optional<AuthSession> authSession =
                authSessionRepository.findById(
                        refreshToken.get().getSessionId()
                );

        if (authSession.isEmpty()) {
            return;
        }

        AuthSession session = authSession.get();

        if (session.isRevoked()) {
            return;
        }

        session.revoke(clock.instant());

        authSessionRepository.save(session);
    }
}
