package com.spsk1313.identityservice.identity.application.service;

import com.spsk1313.identityservice.identity.application.command.RegisterUserCommand;
import com.spsk1313.identityservice.identity.application.exception.DuplicateEmailException;
import com.spsk1313.identityservice.identity.application.policy.PasswordPolicy;
import com.spsk1313.identityservice.identity.application.port.in.EmailVerificationIssuer;
import com.spsk1313.identityservice.identity.application.port.in.RoleAssigner;
import com.spsk1313.identityservice.identity.application.port.out.PasswordHasher;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final EmailVerificationIssuer emailVerificationIssuer;
    private final RoleAssigner roleAssigner;

    public RegisterUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            PasswordPolicy passwordPolicy,
            EmailVerificationIssuer emailVerificationIssuer,
            RoleAssigner roleAssigner
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.emailVerificationIssuer = emailVerificationIssuer;
        this.roleAssigner = roleAssigner;
    }

    @Transactional
    public RegisterUserResult register(RegisterUserCommand command) {
        EmailAddress email =
                new EmailAddress(command.email());

        passwordPolicy.validate(command.password());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        String hashedPassword =
                passwordHasher.hash(command.password());

        User user =
                User.register(email, hashedPassword);

        user = userRepository.save(user);

        roleAssigner.assign(
                user.getId(),
                RoleName.USER
        );

        emailVerificationIssuer.issue(
                user.getId(),
                user.getEmail().value()
        );

        return new RegisterUserResult(
                user.getId(),
                user.getEmail().value(),
                user.isEmailVerified(),
                user.getAccountStatus()
        );
    }
}