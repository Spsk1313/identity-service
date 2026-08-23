package com.spsk1313.identityservice.identity.application.result;

public record LoginResult(
        Long userId,
        String email,
        String accessToken,
        String refreshToken
) {
}
