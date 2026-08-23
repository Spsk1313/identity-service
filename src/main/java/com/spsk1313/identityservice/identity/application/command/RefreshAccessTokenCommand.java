package com.spsk1313.identityservice.identity.application.command;

public record RefreshAccessTokenCommand(
        String refreshToken
) {
}