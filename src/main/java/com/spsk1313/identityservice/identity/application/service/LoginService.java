package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.LoginCommand;
import com.spsk1313.identityservice.identity.application.exception.AccountDisabledException;
import com.spsk1313.identityservice.identity.application.exception.EmailNotVerifiedException;
import com.spsk1313.identityservice.identity.application.exception.InvalidCredentialsException;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public LoginService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public LoginResult login(LoginCommand command) {
        EmailAddress email = new EmailAddress(command.email());

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if(!passwordHasher.matches(command.password(), user.getPasswordHash())) throw new InvalidCredentialsException();

        if(!user.isEmailVerified()) throw new EmailNotVerifiedException();

        if(user.getAccountStatus() == AccountStatus.DISABLED) throw new AccountDisabledException();

        return new LoginResult(user.getId(), user.getEmail().value());
    }
}
