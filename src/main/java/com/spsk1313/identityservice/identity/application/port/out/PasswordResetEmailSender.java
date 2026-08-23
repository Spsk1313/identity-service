package com.spsk1313.identityservice.identity.application.port.out;

public interface PasswordResetEmailSender {

    void sendPasswordResetEmail(
            String email,
            String rawToken
    );
}
