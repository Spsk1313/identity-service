package com.spsk1313.identityservice.identity.application.policy;

import com.spsk1313.identityservice.identity.application.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 64;

    public void validate(String rawPassword) {
        if(rawPassword == null) throw new InvalidPasswordException("Password cannot be null");
        if(rawPassword.isBlank()) throw new InvalidPasswordException("Password cannot be blank");
        if(rawPassword.length() < MIN_PASSWORD_LENGTH || rawPassword.length() > MAX_PASSWORD_LENGTH)
            throw new InvalidPasswordException("Password length must be between %d and %d characters".formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
    }
}
