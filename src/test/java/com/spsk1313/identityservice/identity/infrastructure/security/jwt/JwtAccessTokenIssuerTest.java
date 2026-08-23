package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.spsk1313.identityservice.identity.domain.authorization.PermissionName;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtAccessTokenIssuerTest {

    private static final Instant NOW =
            Instant.parse("2026-08-23T12:00:00Z");

    private static final Duration ACCESS_TOKEN_TTL =
            Duration.ofMinutes(15);

    private static final Long USER_ID = 42L;

    private static final String EMAIL =
            "sahil@example.com";

    private static final String ISSUER =
            "http://localhost:8080";

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private JwtAccessTokenIssuer accessTokenIssuer;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() throws Exception {
        KeyPair keyPair = generateKeyPair();

        publicKey =
                (RSAPublicKey) keyPair.getPublic();

        privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        JwtEncoder jwtEncoder =
                createEncoder(
                        publicKey,
                        privateKey
                );

        Clock clock =
                Clock.fixed(
                        NOW,
                        ZoneOffset.UTC
                );

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .build();

        JwtTimestampValidator timestampValidator =
                new JwtTimestampValidator();

        timestampValidator.setClock(clock);

        decoder.setJwtValidator(
                timestampValidator
        );

        jwtDecoder = decoder;

        JwtProperties properties =
                new JwtProperties(
                        ISSUER,
                        ACCESS_TOKEN_TTL,
                        null,
                        null
                );

        accessTokenIssuer =
                new JwtAccessTokenIssuer(
                        jwtEncoder,
                        properties,
                        clock
                );
    }

    @Test
    void shouldIssueValidSignedAccessToken() {
        UserAuthorization authorization =
                userAuthorization();

        String token =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        Jwt decoded =
                jwtDecoder.decode(token);

        assertEquals(
                USER_ID.toString(),
                decoded.getSubject()
        );

        assertEquals(
                ISSUER,
                decoded.getIssuer().toString()
        );

        assertEquals(
                EMAIL,
                decoded.getClaimAsString("email")
        );

        assertEquals(
                NOW,
                decoded.getIssuedAt()
        );

        assertEquals(
                NOW.plus(ACCESS_TOKEN_TTL),
                decoded.getExpiresAt()
        );

        assertNotNull(decoded.getId());

        assertEquals(
                "RS256",
                decoded.getHeaders().get("alg")
        );
    }

    @Test
    void shouldIncludeRolesAndPermissionsInAccessToken() {
        UserAuthorization authorization =
                UserAuthorization.of(
                        USER_ID,
                        Set.of(
                                RoleName.ADMIN
                        ),
                        Set.of(
                                PermissionName.USER_READ,
                                PermissionName.USER_DISABLE,
                                PermissionName.SESSION_REVOKE,
                                PermissionName.ROLE_ASSIGN
                        )
                );

        String token =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        Jwt decoded =
                jwtDecoder.decode(token);

        List<String> roles =
                decoded.getClaimAsStringList(
                        "roles"
                );

        List<String> permissions =
                decoded.getClaimAsStringList(
                        "permissions"
                );

        assertEquals(
                Set.of("ADMIN"),
                Set.copyOf(roles)
        );

        assertEquals(
                Set.of(
                        "USER_READ",
                        "USER_DISABLE",
                        "SESSION_REVOKE",
                        "ROLE_ASSIGN"
                ),
                Set.copyOf(permissions)
        );
    }

    @Test
    void shouldIncludeEmptyPermissionsWhenUserHasNoPermissions() {
        UserAuthorization authorization =
                userAuthorization();

        String token =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        Jwt decoded =
                jwtDecoder.decode(token);

        assertEquals(
                List.of("USER"),
                decoded.getClaimAsStringList(
                        "roles"
                )
        );

        assertEquals(
                List.of(),
                decoded.getClaimAsStringList(
                        "permissions"
                )
        );
    }

    @Test
    void shouldGenerateDifferentJwtIdForEachAccessToken() {
        UserAuthorization authorization =
                userAuthorization();

        String firstToken =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        String secondToken =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        Jwt first =
                jwtDecoder.decode(firstToken);

        Jwt second =
                jwtDecoder.decode(secondToken);

        assertNotNull(first.getId());
        assertNotNull(second.getId());

        assertNotEquals(
                first.getId(),
                second.getId()
        );

        assertNotEquals(
                firstToken,
                secondToken
        );
    }

    @Test
    void shouldRejectTokenWhenVerifiedWithDifferentPublicKey()
            throws Exception {

        UserAuthorization authorization =
                userAuthorization();

        String token =
                accessTokenIssuer.issue(
                        USER_ID,
                        EMAIL,
                        authorization
                );

        KeyPair differentKeyPair =
                generateKeyPair();

        RSAPublicKey differentPublicKey =
                (RSAPublicKey)
                        differentKeyPair.getPublic();

        JwtDecoder wrongDecoder =
                NimbusJwtDecoder
                        .withPublicKey(
                                differentPublicKey
                        )
                        .build();

        assertThrows(
                JwtException.class,
                () -> wrongDecoder.decode(token)
        );
    }

    private UserAuthorization userAuthorization() {
        return UserAuthorization.of(
                USER_ID,
                Set.of(RoleName.USER),
                Set.of()
        );
    }

    private KeyPair generateKeyPair()
            throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance(
                        "RSA"
                );

        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    private JwtEncoder createEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {
        RSAKey rsaKey =
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .build();

        JWKSet jwkSet =
                new JWKSet(rsaKey);

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<SecurityContext>(
                        jwkSet
                )
        );
    }
}