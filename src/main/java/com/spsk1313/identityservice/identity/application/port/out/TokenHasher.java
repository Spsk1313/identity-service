package com.spsk1313.identityservice.identity.application.port.out;

public interface TokenHasher {

    String hash(String rawToken);
}