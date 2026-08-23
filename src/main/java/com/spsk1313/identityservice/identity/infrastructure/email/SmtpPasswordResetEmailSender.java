package com.spsk1313.identityservice.identity.infrastructure.email;

import com.spsk1313.identityservice.identity.application.port.out.PasswordResetEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetEmailSender
        implements PasswordResetEmailSender {

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public SmtpPasswordResetEmailSender(
            JavaMailSender mailSender,
            @Value("${app.verification.base-url}") String baseUrl
    ) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendPasswordResetEmail(
            String email,
            String rawToken
    ) {
        String resetLink =
                "%s/api/auth/reset-password?token=%s"
                        .formatted(baseUrl, rawToken);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom("no-reply@identity.local");
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText("""
                Please reset your password using the following link:

                %s

                This link expires in 30 minutes.
                """.formatted(resetLink));

        mailSender.send(message);
    }
}
