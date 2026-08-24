package com.spsk1313.identityservice.identity.web.controller;

import com.spsk1313.identityservice.identity.application.port.in.AdminUserReader;
import com.spsk1313.identityservice.identity.application.port.in.UserDisabler;
import com.spsk1313.identityservice.identity.application.port.in.UserRoleAssigner;
import com.spsk1313.identityservice.identity.application.port.in.UserSessionRevoker;
import com.spsk1313.identityservice.identity.application.result.AdminUserResult;
import com.spsk1313.identityservice.identity.domain.AccountStatus;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.infrastructure.security.SecurityConfiguration;
import com.spsk1313.identityservice.identity.infrastructure.security.jwt.JwtAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({
        SecurityConfiguration.class,
        JwtAuthoritiesConverter.class
})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserReader adminUserReader;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private UserDisabler userDisabler;

    @MockitoBean
    private UserSessionRevoker userSessionRevoker;

    @MockitoBean
    private UserRoleAssigner userRoleAssigner;

    private static final Long USER_ID = 42L;

    @Test
    void shouldReturnUserWhenCallerHasUserReadPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("MODERATOR"),
                List.of(
                        "USER_READ",
                        "SESSION_REVOKE"
                )
        );

        when(jwtDecoder.decode("moderator-token"))
                .thenReturn(jwt);

        AdminUserResult result =
                new AdminUserResult(
                        USER_ID,
                        "sahil@example.com",
                        true,
                        AccountStatus.ACTIVE
                );

        when(adminUserReader.getById(USER_ID))
                .thenReturn(result);

        mockMvc.perform(
                        get("/api/admin/users/{userId}", USER_ID)
                                .header(
                                        "Authorization",
                                        "Bearer moderator-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(USER_ID)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("sahil@example.com")
                )
                .andExpect(
                        jsonPath("$.emailVerified")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.accountStatus")
                                .value("ACTIVE")
                );

        verify(adminUserReader)
                .getById(USER_ID);
    }

    @Test
    void shouldReturnForbiddenWhenCallerDoesNotHaveUserReadPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("USER"),
                List.of()
        );

        when(jwtDecoder.decode("user-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/api/admin/users/{userId}", USER_ID)
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(adminUserReader);
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/admin/users/{userId}",
                                USER_ID
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(adminUserReader);
    }

    private Jwt jwt(
            List<String> roles,
            List<String> permissions
    ) {
        Instant now =
                Instant.parse(
                        "2026-08-23T12:00:00Z"
                );

        return new Jwt(
                "test-token",
                now,
                now.plusSeconds(900),
                Map.of(
                        "alg",
                        "RS256"
                ),
                Map.of(
                        "sub",
                        "1",
                        "email",
                        "admin@example.com",
                        "roles",
                        roles,
                        "permissions",
                        permissions
                )
        );
    }

    @Test
    void shouldDisableUserWhenCallerHasUserDisablePermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("ADMIN"),
                List.of(
                        "USER_READ",
                        "USER_DISABLE",
                        "SESSION_REVOKE",
                        "ROLE_ASSIGN"
                )
        );

        when(jwtDecoder.decode("admin-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        patch(
                                "/api/admin/users/{userId}/disable",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isNoContent());

        verify(userDisabler)
                .disable(USER_ID);
    }

    @Test
    void shouldRevokeAllUserSessionsWhenCallerHasSessionRevokePermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("MODERATOR"),
                List.of(
                        "USER_READ",
                        "SESSION_REVOKE"
                )
        );

        when(jwtDecoder.decode("moderator-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/sessions/revoke",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer moderator-token"
                                )
                )
                .andExpect(status().isNoContent());

        verify(userSessionRevoker)
                .revokeAll(USER_ID);
    }

    @Test
    void shouldReturnForbiddenWhenCallerDoesNotHaveSessionRevokePermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("USER"),
                List.of()
        );

        when(jwtDecoder.decode("user-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/sessions/revoke",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(userSessionRevoker);
    }

    @Test
    void shouldReturnUnauthorizedWhenRevokingSessionsWithoutAccessToken()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/sessions/revoke",
                                USER_ID
                        ).with(csrf())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userSessionRevoker);
    }

    @Test
    void shouldAssignRoleWhenCallerHasRoleAssignPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("ADMIN"),
                List.of("ROLE_ASSIGN")
        );

        when(jwtDecoder.decode("admin-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/roles",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "role": "MODERATOR"
                                    }
                                    """)
                )
                .andExpect(status().isNoContent());

        verify(userRoleAssigner)
                .assign(
                        USER_ID,
                        RoleName.MODERATOR
                );
    }

    @Test
    void shouldReturnForbiddenWhenCallerDoesNotHaveRoleAssignPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("MODERATOR"),
                List.of(
                        "USER_READ",
                        "SESSION_REVOKE"
                )
        );

        when(jwtDecoder.decode("moderator-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/roles",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer moderator-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "role": "ADMIN"
                                    }
                                    """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRoleAssigner);
    }

    @Test
    void shouldReturnUnauthorizedWhenAssigningRoleWithoutAccessToken()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/roles",
                                USER_ID
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "role": "MODERATOR"
                                    }
                                    """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRoleAssigner);
    }

    @Test
    void shouldRejectRoleAssignmentWhenRoleIsMissing()
            throws Exception {

        Jwt jwt = jwt(
                List.of("ADMIN"),
                List.of("ROLE_ASSIGN")
        );

        when(jwtDecoder.decode("admin-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        post(
                                "/api/admin/users/{userId}/roles",
                                USER_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {}
                                    """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userRoleAssigner);
    }
}
