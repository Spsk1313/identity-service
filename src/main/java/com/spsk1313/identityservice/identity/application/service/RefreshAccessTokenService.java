package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.RefreshAccessTokenCommand;
import com.spsk1313.identityservice.identity.application.exception.InvalidRefreshTokenException;
import com.spsk1313.identityservice.identity.application.port.out.*;
import com.spsk1313.identityservice.identity.application.result.RefreshAccessTokenResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.auth.AuthSession;
import com.spsk1313.identityservice.identity.domain.auth.RefreshToken;
import com.spsk1313.identityservice.identity.domain.auth.RefreshTokenAlreadyUsedException;
import com.spsk1313.identityservice.identity.domain.auth.RefreshTokenExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class RefreshAccessTokenService {

    private final RevokeAuthSessionService revokeAuthSessionService;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;

    private final TokenHasher tokenHasher;
    private final RawTokenGenerator tokenGenerator;
    private final AccessTokenIssuer accessTokenIssuer;

    private final Clock clock;

    public RefreshAccessTokenService(
            RevokeAuthSessionService revokeAuthSessionService,
            RefreshTokenRepository refreshTokenRepository,
            AuthSessionRepository authSessionRepository,
            UserRepository userRepository,
            TokenHasher tokenHasher,
            RawTokenGenerator tokenGenerator,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock
    ) {
        this.revokeAuthSessionService = revokeAuthSessionService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
        this.tokenGenerator = tokenGenerator;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public RefreshAccessTokenResult refresh(
            RefreshAccessTokenCommand command
    ) {
        Instant now = clock.instant();

        String tokenHash = tokenHasher.hash(command.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        AuthSession session = authSessionRepository
                .findById(refreshToken.getSessionId())
                .orElseThrow(InvalidRefreshTokenException::new);

        try {
            refreshToken.use(now);
        } catch (RefreshTokenAlreadyUsedException ex) {
            revokeAuthSessionService.revoke(session);

            throw new InvalidRefreshTokenException();
        } catch (RefreshTokenExpiredException ex) {
            throw new InvalidRefreshTokenException();
        }

        if(!session.isActive(now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository
                .findById(session.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        if(user.getAccountStatus() == AccountStatus.DISABLED) {
            revokeAuthSessionService.revoke(session);

            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.save(refreshToken);

        session.markUsed(now);
        authSessionRepository.save(session);

        String newRawRefreshToken = tokenGenerator.generate();

        String newRefreshTokenHash = tokenHasher.hash(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.issue(
                session.getId(),
                newRefreshTokenHash,
                session.getExpiresAt()
        );

        refreshTokenRepository.save(newRefreshToken);

        String newAccessToken = accessTokenIssuer.issue(
                user.getId(),
                user.getEmail().value()
        );

        return new RefreshAccessTokenResult(
                newAccessToken,
                newRawRefreshToken
        );
    }
}
