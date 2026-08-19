package com.spsk1313.identityservice.identity.application.port.out;

public interface VerificationEmailSender {

    void sendVerificationEmail(
            String email,
            String verificationLink
    );
}
