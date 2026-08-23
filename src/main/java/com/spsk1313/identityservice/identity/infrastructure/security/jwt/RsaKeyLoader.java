package com.spsk1313.identityservice.identity.infrastructure.security.jwt;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RsaKeyLoader {

    private RsaKeyLoader() {
    }

    public static RSAPrivateKey loadPrivateKey(Resource resource) {
        try {
            String pem = read(resource);

            String encoded = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(encoded);

            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");

            return (RSAPrivateKey)
                    keyFactory.generatePrivate(keySpec);

        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to load RSA private key",
                    e
            );
        }
    }

    public static RSAPublicKey loadPublicKey(Resource resource) {
        try {
            String pem = read(resource);

            String encoded = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(encoded);

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");

            return (RSAPublicKey)
                    keyFactory.generatePublic(keySpec);

        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to load RSA public key",
                    e
            );
        }
    }

    private static String read(Resource resource) throws IOException {
        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }
}