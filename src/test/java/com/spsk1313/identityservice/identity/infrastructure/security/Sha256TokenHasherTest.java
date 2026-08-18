package com.spsk1313.identityservice.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Sha256TokenHasherTest {

    private static final String TOKEN = "token";

    @Test
    void shouldProduceDeterministicSha256Hash() {
        Sha256TokenHasher hasher = new Sha256TokenHasher();

        String hash1 = hasher.hash(TOKEN);
        String hash2 = hasher.hash(TOKEN);

        assertEquals(hash1, hash2);
    }

    @Test
    void shouldProduce64CharacterHexHash() {
        Sha256TokenHasher hasher = new Sha256TokenHasher();

        String hash = hasher.hash(TOKEN);

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }
}
