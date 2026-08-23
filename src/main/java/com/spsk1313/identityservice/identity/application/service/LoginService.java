package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.LoginCommand;
import com.spsk1313.identityservice.identity.application.exception.AccountDisabledException;
import com.spsk1313.identityservice.identity.application.exception.EmailNotVerifiedException;
import com.spsk1313.identityservice.identity.application.exception.InvalidCredentialsException;
import com.spsk1313.identityservice.identity.application.port.out.*;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;
import com.spsk1313.identityservice.identity.domain.authentication.RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthSessionRepository authSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RawTokenGenerator rawTokenGenerator;
    private final TokenHasher tokenHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    private static final Duration SESSION_TTL = Duration.ofDays(30);

    public LoginService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthSessionRepository authSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RawTokenGenerator rawTokenGenerator,
            TokenHasher tokenHasher,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authSessionRepository = authSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.rawTokenGenerator = rawTokenGenerator;
        this.tokenHasher = tokenHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        EmailAddress email = new EmailAddress(command.email());

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if(!passwordHasher.matches(command.password(), user.getPasswordHash())) throw new InvalidCredentialsException();

        if(!user.isEmailVerified()) throw new EmailNotVerifiedException();

        if(user.getAccountStatus() == AccountStatus.DISABLED) throw new AccountDisabledException();

        Instant now = clock.instant();

        AuthSession session = AuthSession.start(user.getId(), now.plus(SESSION_TTL), command.userAgent());

        session = authSessionRepository.save(session);

        String rawRefreshToken = rawTokenGenerator.generate();

        String refreshTokenHash = tokenHasher.hash(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.issue(
                session.getId(),
                refreshTokenHash,
                session.getExpiresAt()
        );

        refreshTokenRepository.save(refreshToken);

        String accessToken = accessTokenIssuer.issue(user.getId(), user.getEmail().value());

        return new LoginResult(user.getId(), user.getEmail().value(), accessToken, rawRefreshToken);
    }
}
