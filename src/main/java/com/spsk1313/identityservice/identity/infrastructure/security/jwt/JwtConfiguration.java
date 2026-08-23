package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    @Bean
    RSAPrivateKey rsaPrivateKey(JwtProperties properties) {
        return RsaKeyLoader.loadPrivateKey(
                properties.privateKey()
        );
    }

    @Bean
    RSAPublicKey rsaPublicKey(JwtProperties properties) {
        return RsaKeyLoader.loadPublicKey(
                properties.publicKey()
        );
    }

    @Bean
    JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<SecurityContext>(jwkSet)
        );
    }

    @Bean
    JwtDecoder jwtDecoder(
            RSAPublicKey publicKey,
            JwtProperties properties
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();

        OAuth2TokenValidator<Jwt> validator =
                JwtValidators.createDefaultWithIssuer(
                        properties.issuer()
                );

        decoder.setJwtValidator(validator);

        return decoder;
    }
}
