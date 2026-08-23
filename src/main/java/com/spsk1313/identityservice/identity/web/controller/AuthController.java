package com.spsk1313.identityservice.identity.web.controller;

import com.spsk1313.identityservice.identity.application.command.*;
import com.spsk1313.identityservice.identity.application.exception.InvalidRefreshTokenException;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.application.result.RefreshAccessTokenResult;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.application.service.*;
import com.spsk1313.identityservice.identity.web.cookie.RefreshTokenCookieFactory;
import com.spsk1313.identityservice.identity.web.request.ForgotPasswordRequest;
import com.spsk1313.identityservice.identity.web.request.LoginRequest;
import com.spsk1313.identityservice.identity.web.request.RegisterUserRequest;
import com.spsk1313.identityservice.identity.web.request.ResetPasswordRequest;
import com.spsk1313.identityservice.identity.web.response.LoginResponse;
import com.spsk1313.identityservice.identity.web.response.RefreshAccessTokenResponse;
import com.spsk1313.identityservice.identity.web.response.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final RegisterUserService registerUserService;
    private final VerifyEmailService verifyEmailService;
    private final LoginService loginService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final RefreshAccessTokenService refreshAccessTokenService;
    private final LogoutService logoutService;
    private final LogoutAllService logoutAllService;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(
            RegisterUserService registerUserService,
            VerifyEmailService verifyEmailService,
            LoginService loginService,
            RefreshTokenCookieFactory refreshTokenCookieFactory,
            RefreshAccessTokenService refreshAccessTokenService,
            LogoutService logoutService,
            LogoutAllService logoutAllService,
            ForgotPasswordService forgotPasswordService,
            ResetPasswordService resetPasswordService
    ) {
        this.registerUserService = registerUserService;
        this.verifyEmailService = verifyEmailService;
        this.loginService = loginService;
        this.refreshTokenCookieFactory = refreshTokenCookieFactory;
        this.refreshAccessTokenService = refreshAccessTokenService;
        this.logoutService = logoutService;
        this.logoutAllService = logoutAllService;
        this.forgotPasswordService = forgotPasswordService;
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest req) {
        RegisterUserCommand command = new RegisterUserCommand(req.email(), req.password());
        RegisterUserResult result = registerUserService.register(command);
        RegisterUserResponse response = new RegisterUserResponse(result.id(), result.email(), result.emailVerified(), result.accountStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam String token
    ) {
        verifyEmailService.verify(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
            ) {
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password(),
                userAgent
        );

        LoginResult result = loginService.login(command);

        LoginResponse response = new LoginResponse(
                result.userId(),
                result.email(),
                result.accessToken()
        );

        ResponseCookie refreshCookie = refreshTokenCookieFactory.create(result.refreshToken());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshAccessTokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        RefreshAccessTokenResult result =
                refreshAccessTokenService.refresh(
                        new RefreshAccessTokenCommand(refreshToken)
                );

        ResponseCookie refreshTokenCookie =
                refreshTokenCookieFactory.create(
                        result.refreshToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(
                        new RefreshAccessTokenResponse(
                                result.accessToken()
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            ) String refreshToken
    ) {
        logoutService.logout(refreshToken);

        ResponseCookie clearedCookie =
                refreshTokenCookieFactory.clear();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearedCookie.toString()
                )
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal Jwt jwt
    ) {

        String subject = jwt.getSubject();

        if(subject == null || subject.isBlank()) {
            throw new IllegalStateException("Authenticated JWT is missing subject");
        }

        Long userId = Long.valueOf(subject);

        logoutAllService.logoutAll(userId);

        ResponseCookie clearedCookie =
                refreshTokenCookieFactory.clear();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearedCookie.toString()
                )
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        forgotPasswordService.forgotPassword(
                new ForgotPasswordCommand(
                        request.email()
                )
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        resetPasswordService.resetPassword(
                new ResetPasswordCommand(
                        request.token(),
                        request.newPassword()
                )
        );

        return ResponseEntity.noContent().build();
    }

}
