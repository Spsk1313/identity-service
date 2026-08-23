package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import com.spsk1313.identityservice.identity.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestAuthorizationController.class)
@Import({
        SecurityConfiguration.class,
        JwtAuthoritiesConverter.class
})
class JwtAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturnUnauthorizedWhenJwtIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/test/security/user")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldAllowUserRoleToAccessUserEndpoint()
            throws Exception {

        Jwt jwt = jwt(
                List.of("USER"),
                List.of()
        );

        when(jwtDecoder.decode("user-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/test/security/user")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("user")
                );
    }

    @Test
    void shouldDenyUserRoleFromAdminEndpoint()
            throws Exception {

        Jwt jwt = jwt(
                List.of("USER"),
                List.of()
        );

        when(jwtDecoder.decode("user-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/test/security/admin")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void shouldAllowAdminRoleToAccessAdminEndpoint()
            throws Exception {

        Jwt jwt = jwt(
                List.of("ADMIN"),
                List.of()
        );

        when(jwtDecoder.decode("admin-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/test/security/admin")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("admin")
                );
    }

    @Test
    void shouldDenyUserWithoutRequiredPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("USER"),
                List.of()
        );

        when(jwtDecoder.decode("user-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/test/security/user-disable")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void shouldAllowUserWithRequiredPermission()
            throws Exception {

        Jwt jwt = jwt(
                List.of("ADMIN"),
                List.of("USER_DISABLE")
        );

        when(jwtDecoder.decode("admin-token"))
                .thenReturn(jwt);

        mockMvc.perform(
                        get("/test/security/user-disable")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "user-disable"
                        )
                );
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
                        "42",
                        "roles",
                        roles,
                        "permissions",
                        permissions
                )
        );
    }
}