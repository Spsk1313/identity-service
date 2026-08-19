package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.exception.UserNotFoundException;
import com.spsk1313.identityservice.identity.application.exception.VerificationTokenNotFoundException;
import com.spsk1313.identityservice.identity.application.port.out.EmailVerificationTokenRepository;
import com.spsk1313.identityservice.identity.application.port.out.TokenHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.verification.EmailVerificationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class VerifyEmailService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TokenHasher tokenHasher;
    private final Clock clock;

    public VerifyEmailService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            TokenHasher tokenHasher,
            Clock clock) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
    }

    @Transactional
    public void verify(String rawToken) {
        String hash = tokenHasher.hash(rawToken);

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash).orElseThrow(VerificationTokenNotFoundException::new);

        var now = clock.instant();
        token.use(now);

        User user = userRepository.findById(token.getUserId()).orElseThrow(UserNotFoundException::new);

        user.verifyEmail();

        tokenRepository.save(token);

        userRepository.save(user);

    }
}
