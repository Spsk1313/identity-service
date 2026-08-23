package com.spsk1313.identityservice.identity.application.command;

public record ResetPasswordCommand(
        String token,
        String newPassword
) {
}