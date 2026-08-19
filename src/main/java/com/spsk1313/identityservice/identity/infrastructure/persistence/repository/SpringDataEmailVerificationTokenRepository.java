package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataEmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenJpaEntity, Long> {

    Optional<EmailVerificationTokenJpaEntity> findByTokenHash(String tokenHash);

    Optional<EmailVerificationTokenJpaEntity> findByUser_IdAndUsedAtIsNullAndInvalidatedAtIsNull(Long userId);

}
