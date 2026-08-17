package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    boolean existsByEmail(String email);
}
