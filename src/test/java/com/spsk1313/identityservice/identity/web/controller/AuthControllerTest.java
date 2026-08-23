package com.spsk1313.identityservice.identity.web.controller;

import com.spsk1313.identityservice.identity.application.command.*;
import com.spsk1313.identityservice.identity.application.exception.*;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.application.service.*;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.web.cookie.RefreshTokenCookieFactory;
import com.spsk1313.identityservice.identity.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.spsk1313.identityservice.identity.application.result.RefreshAccessTokenResult;

import jakarta.servlet.http.Cookie;

import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.containsString;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterUserService registerUserService;

    @Mock
    private VerifyEmailService verifyEmailService;

    @Mock
    private LoginService loginService;

    @Mock
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

    @Mock
    private RefreshAccessTokenService refreshAccessTokenService;

    @Mock
    private LogoutService logoutService;

    @Mock
    private ForgotPasswordService forgotPasswordService;

    @Mock
    private ResetPasswordService resetPasswordService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserAndReturn201() throws Exception {
        RegisterUserResult response =
                new RegisterUserResult(
                        1L,
                        "sahil@example.com",
                        false,
                        AccountStatus.ACTIVE
                );

        when(registerUserService.register(any(RegisterUserCommand.class)))
                .thenReturn(response);

        String json = """
                {
                    "email": "sahil@example.com",
                    "password": "password1234"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("sahil@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    void shouldReturn400WhenRequestValidationFails() throws Exception {
        String json = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(registerUserService);
        verifyNoInteractions(verifyEmailService);
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class)))
                .thenThrow(new DuplicateEmailException());

        String json = """
                {
                    "email": "sahil@example.com",
                    "password": "password1234"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("This email is already in use"));
    }

    @Test
    void shouldReturn400WhenPasswordPolicyRejectsPassword() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class)))
                .thenThrow(
                        new InvalidPasswordException(
                                "Password length must be between 12 and 64 characters"
                        )
                );

        String json = """
                {
                    "email": "sahil@example.com",
                    "password": "password"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Password length must be between 12 and 64 characters"));
    }

    @Test
    void shouldVerifyEmailAndReturn204() throws Exception {
        String rawToken = "raw-verification-token";

        mockMvc.perform(
                        post("/api/auth/verify-email")
                                .param("token", rawToken)
                )
                .andExpect(status().isNoContent());

        verify(verifyEmailService).verify(rawToken);
    }

    @Test
    void shouldReturn400ForInvalidVerificationToken() throws Exception {
        String rawToken = "invalid-token";

        doThrow(new VerificationTokenNotFoundException())
                .when(verifyEmailService)
                .verify(rawToken);

        mockMvc.perform(
                        post("/api/auth/verify-email")
                                .param("token", rawToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired verification token"));
    }

    @Test
    void shouldLoginAndReturnAccessTokenAndRefreshTokenCookie() throws Exception {
        LoginResult result = new LoginResult(
                1L,
                "sahil@example.com",
                "access-token",
                "raw-refresh-token"
        );

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "raw-refresh-token")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(30))
                .build();

        when(loginService.login(any(LoginCommand.class)))
                .thenReturn(result);

        when(refreshTokenCookieFactory.create("raw-refresh-token"))
                .thenReturn(refreshCookie);

        String json = """
            {
                "email": "sahil@example.com",
                "password": "correct-horse-battery-staple"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .header("User-Agent", "Test Browser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("sahil@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))

                .andExpect(jsonPath("$.refreshToken").doesNotExist())

                .andExpect(cookie().value(
                        "refresh_token",
                        "raw-refresh-token"
                ))
                .andExpect(cookie().httpOnly(
                        "refresh_token",
                        true
                ))
                .andExpect(cookie().path(
                        "refresh_token",
                        "/api/auth"
                ));

        verify(loginService).login(
                new LoginCommand(
                        "sahil@example.com",
                        "correct-horse-battery-staple",
                        "Test Browser"
                )
        );

        verify(refreshTokenCookieFactory)
                .create("raw-refresh-token");
    }

    @Test
    void shouldReturn400WhenLoginRequestValidationFails() throws Exception {
        String json = """
            {
                "email": "",
                "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(loginService);
        verifyNoInteractions(refreshTokenCookieFactory);
    }

    @Test
    void shouldRefreshAccessTokenAndRotateRefreshTokenCookie() throws Exception {
        String currentRefreshToken = "refresh-token-r1";
        String newAccessToken = "access-token-a2";
        String newRefreshToken = "refresh-token-r2";

        when(refreshAccessTokenService.refresh(
                new RefreshAccessTokenCommand(currentRefreshToken)
        )).thenReturn(
                new RefreshAccessTokenResult(
                        newAccessToken,
                        newRefreshToken
                )
        );

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(30))
                .build();

        when(refreshTokenCookieFactory.create(newRefreshToken))
                .thenReturn(cookie);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                currentRefreshToken
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken")
                        .doesNotExist())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString(
                                "refresh_token=" + newRefreshToken
                        )
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("SameSite=Lax")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Path=/api/auth")
                ));

        verify(refreshAccessTokenService).refresh(
                new RefreshAccessTokenCommand(currentRefreshToken)
        );

        verify(refreshTokenCookieFactory)
                .create(newRefreshToken);
    }

    @Test
    void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
        String refreshToken = "invalid-refresh-token";

        when(refreshAccessTokenService.refresh(
                new RefreshAccessTokenCommand(refreshToken)
        )).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                refreshToken
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(refreshAccessTokenService).refresh(
                new RefreshAccessTokenCommand(refreshToken)
        );

        verifyNoInteractions(refreshTokenCookieFactory);
    }

    @Test
    void shouldReturn401WhenRefreshTokenCookieIsMissing() throws Exception {
        mockMvc.perform(
                        post("/api/auth/refresh")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(refreshAccessTokenService);
        verifyNoInteractions(refreshTokenCookieFactory);
    }

    @Test
    void shouldLogoutAndClearRefreshTokenCookie() throws Exception {
        String refreshToken = "refresh-token-r1";

        ResponseCookie clearedCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        when(refreshTokenCookieFactory.clear())
                .thenReturn(clearedCookie);

        mockMvc.perform(
                        post("/api/auth/logout")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                refreshToken
                                        )
                                )
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("refresh_token=")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Max-Age=0")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Path=/api/auth")
                ));

        verify(logoutService).logout(refreshToken);
        verify(refreshTokenCookieFactory).clear();
    }

    @Test
    void shouldLogoutSuccessfullyWhenRefreshTokenCookieIsMissing() throws Exception {
        ResponseCookie clearedCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        when(refreshTokenCookieFactory.clear())
                .thenReturn(clearedCookie);

        mockMvc.perform(
                        post("/api/auth/logout")
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("Max-Age=0")
                ));

        verify(logoutService).logout(null);
        verify(refreshTokenCookieFactory).clear();
    }

    @Test
    void shouldAllowAnonymousUserToAccessLogoutEndpoint() throws Exception {
        ResponseCookie clearedCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        when(refreshTokenCookieFactory.clear())
                .thenReturn(clearedCookie);

        mockMvc.perform(
                        post("/api/auth/logout")
                )
                .andExpect(status().isNoContent());

        verify(logoutService).logout(null);
    }

    @Test
    void shouldAcceptForgotPasswordRequest() throws Exception {
        String json = """
            {
                "email": "login-test@example.com"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNoContent());

        verify(forgotPasswordService)
                .forgotPassword(
                        new ForgotPasswordCommand(
                                "login-test@example.com"
                        )
                );
    }

    @Test
    void shouldReturnSameResponseForForgotPasswordRegardlessOfAccountExistence()
            throws Exception {

        String json = """
            {
                "email": "does-not-exist@example.com"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNoContent());

        verify(forgotPasswordService)
                .forgotPassword(
                        new ForgotPasswordCommand(
                                "does-not-exist@example.com"
                        )
                );
    }

    @Test
    void shouldReturn400WhenForgotPasswordEmailIsInvalid()
            throws Exception {

        String json = """
            {
                "email": "not-an-email"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(forgotPasswordService);
    }

    @Test
    void shouldResetPassword() throws Exception {
        String json = """
            {
                "token": "valid-reset-token",
                "newPassword": "new-secure-password"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNoContent());

        verify(resetPasswordService)
                .resetPassword(
                        new ResetPasswordCommand(
                                "valid-reset-token",
                                "new-secure-password"
                        )
                );
    }

    @Test
    void shouldReturn400WhenResetPasswordTokenIsMissing()
            throws Exception {

        String json = """
            {
                "token": "",
                "newPassword": "new-secure-password"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(resetPasswordService);
    }

    @Test
    void shouldReturn400WhenNewPasswordIsTooShort()
            throws Exception {

        String json = """
            {
                "token": "valid-reset-token",
                "newPassword": "short"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(resetPasswordService);
    }

    @Test
    void shouldReturn400WhenPasswordResetTokenIsInvalid()
            throws Exception {

        doThrow(new InvalidPasswordResetTokenException())
                .when(resetPasswordService)
                .resetPassword(any(ResetPasswordCommand.class));

        String json = """
            {
                "token": "invalid-reset-token",
                "newPassword": "new-secure-password"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired password reset token"));
    }

}