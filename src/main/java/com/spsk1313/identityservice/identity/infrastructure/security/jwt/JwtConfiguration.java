package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

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
            RSAPublicKey publicKey
    ) {
        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }
}
