package com.spsk1313.identityservice.identity.web.response;

public record LoginResponse(
        Long userId,
        String email,
        String accessToken
) {
}