package com.spsk1313.identityservice.identity.domain;

import com.spsk1313.identityservice.identity.domain.exception.InvalidEmailException;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final int MAX_EMAIL_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");


    public EmailAddress {
        if(value == null) {
            throw new InvalidEmailException("Email cannot be null");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if(value.isBlank()) {
            throw new InvalidEmailException("Email cannot be blank");
        }

        if(value.length() > MAX_EMAIL_LENGTH) {
            throw new InvalidEmailException("Email cannot have more than " + MAX_EMAIL_LENGTH + " characters");
        }

        if(!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Please enter a valid email");
        }
    }
}
