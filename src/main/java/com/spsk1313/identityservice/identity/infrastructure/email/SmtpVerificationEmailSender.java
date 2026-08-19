package com.spsk1313.identityservice.identity.infrastructure.email;

import com.spsk1313.identityservice.identity.application.port.out.VerificationEmailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpVerificationEmailSender implements VerificationEmailSender {

    private final JavaMailSender mailSender;

    public SmtpVerificationEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String email, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@identity.local");
        message.setTo(email);
        message.setSubject("Verify your email");
        message.setText("""
                Please verify your email using the following link:
                
                %s
                
                This link expires in 24 hours.
                """.formatted(verificationLink));

        mailSender.send(message);

    }
}
