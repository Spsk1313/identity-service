package com.spsk1313.identityservice.identity.application.command;

public record LoginCommand(
        String email,
        String password,
        String userAgent
) {
}
