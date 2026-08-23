package com.spsk1313.identityservice.identity.application.port.out;

import com.spsk1313.identityservice.identity.domain.authentication.AuthSession;

import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository {

    AuthSession save(AuthSession session);

    Optional<AuthSession> findById(Long id);

    List<AuthSession> findByUserId(Long userId);
}