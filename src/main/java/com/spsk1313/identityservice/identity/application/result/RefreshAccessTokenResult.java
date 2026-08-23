package com.spsk1313.identityservice.identity.application.result;

public record RefreshAccessTokenResult(
        String accessToken,
        String refreshToken
) {
}