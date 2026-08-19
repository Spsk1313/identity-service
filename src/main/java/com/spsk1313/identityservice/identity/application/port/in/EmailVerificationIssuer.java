package com.spsk1313.identityservice.identity.application.port.in;

public interface EmailVerificationIssuer {
    void issue(Long userId, String email);
}
