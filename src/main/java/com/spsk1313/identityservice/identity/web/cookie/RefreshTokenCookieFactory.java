package com.spsk1313.identityservice.identity.web.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieFactory {

    private static final String COOKIE_NAME = "refresh_token";
    private static final Duration MAX_AGE = Duration.ofDays(30);

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(MAX_AGE)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie
                .from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

}
