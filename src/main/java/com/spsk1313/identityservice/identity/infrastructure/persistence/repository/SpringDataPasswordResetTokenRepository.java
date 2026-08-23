package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;


import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SpringDataPasswordResetTokenRepository
        extends JpaRepository<PasswordResetTokenJpaEntity, Long> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
    UPDATE PasswordResetTokenJpaEntity token
       SET token.usedAt = :usedAt
     WHERE token.userId = :userId
       AND token.usedAt IS NULL
""")
    int invalidateAllUnusedByUserId(
            @Param("userId") Long userId,
            @Param("usedAt") Instant usedAt
    );
}
