package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRoleRepository
        extends JpaRepository<RoleJpaEntity, Long> {

    Optional<RoleJpaEntity> findByName(RoleName name);
}
