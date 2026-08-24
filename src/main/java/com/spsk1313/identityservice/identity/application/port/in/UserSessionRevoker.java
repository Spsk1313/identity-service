package com.spsk1313.identityservice.identity.application.port.in;

public interface UserSessionRevoker {

    void revokeAll(Long userId);
}