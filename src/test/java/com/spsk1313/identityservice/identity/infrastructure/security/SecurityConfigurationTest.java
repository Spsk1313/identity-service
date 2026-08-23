package com.spsk1313.identityservice.identity.infrastructure.security;

import com.spsk1313.identityservice.identity.application.command.LoginCommand;
import com.spsk1313.identityservice.identity.application.command.RegisterUserCommand;
import com.spsk1313.identityservice.identity.application.result.LoginResult;
import com.spsk1313.identityservice.identity.application.result.RegisterUserResult;
import com.spsk1313.identityservice.identity.application.service.LoginService;
import com.spsk1313.identityservice.identity.application.service.RegisterUserService;
import com.spsk1313.identityservice.identity.application.service.VerifyEmailService;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.web.controller.AuthController;
import com.spsk1313.identityservice.identity.web.cookie.RefreshTokenCookieFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class SecurityConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private RegisterUserService registerUserService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private VerifyEmailService verifyEmailService;

    @MockitoBean
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

    @MockitoBean
    private JwtDecoder jwtDecoder;


    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldAllowAnonymousUserToAccessRegistrationEndpoint() throws Exception {
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
                .andExpect(status().isCreated());
    }

    @Test
    void shouldAllowAnonymousUserToVerifyEmail() throws Exception {
        String rawToken = "raw-verification-token";

        mockMvc.perform(
                        post("/api/auth/verify-email")
                                .param("token", rawToken)
                )
                .andExpect(status().isNoContent());

        verify(verifyEmailService).verify(rawToken);
    }

    @Test
    void shouldReturn401WhenAnonymousUserAccessesProtectedEndpoint()
            throws Exception {

        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAnonymousUserToAccessLoginEndpoint() throws Exception {
        LoginResult result = new LoginResult(
                1L,
                "sahil@example.com",
                "access-token",
                "raw-refresh-token"
        );

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", "raw-refresh-token")
                .httpOnly(true)
                .path("/api/auth")
                .build();

        when(loginService.login(any(LoginCommand.class)))
                .thenReturn(result);

        when(refreshTokenCookieFactory.create("raw-refresh-token"))
                .thenReturn(cookie);

        String json = """
            {
                "email": "sahil@example.com",
                "password": "correct-horse-battery-staple"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAuthenticatedJwtUserToAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(
                        get("/api/protected")
                                .with(jwt()
                                        .jwt(jwt -> jwt
                                                .subject("8")
                                                .claim(
                                                        "email",
                                                        "login-test@example.com"
                                                )
                                        )
                                )
                )
                .andExpect(status().isNotFound());
    }
}