package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.AuthSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuthSessionRepository
        extends JpaRepository<AuthSessionJpaEntity, Long> {
}