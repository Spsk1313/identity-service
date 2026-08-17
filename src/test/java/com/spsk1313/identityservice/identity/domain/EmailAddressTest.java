package com.spsk1313.identityservice.identity.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailAddressTest {

    @Test
    void shouldNormalizeEmailAddress() {
        String input = " Sahil@Example.com ";

        EmailAddress email = new EmailAddress(input);

        assertEquals("sahil@example.com", email.value());
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("   "));
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("abd@de"));
    }

    @Test
    void shouldAcceptEmailWith255chars() {
        String input = "a".repeat(243) + "@example.com";
        EmailAddress email = new EmailAddress(input);
        assertEquals(255, input.length());
        assertEquals(input, email.value());
    }

    @Test
    void shouldRejectMoreThan255Chars() {

        String input = "a".repeat(244) + "@example.com";

        assertEquals(256, input.length());
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(input));
    }

    @Test
    void sameCanonicalEmailObjectsAreEqual() {
        EmailAddress e1 = new EmailAddress(" Sahil@Example.Com ");
        EmailAddress e2 = new EmailAddress("sahil@example.com");
        assertEquals(e1, e2);
    }

}
