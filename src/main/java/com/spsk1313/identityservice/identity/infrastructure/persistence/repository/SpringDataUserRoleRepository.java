package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleId;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRoleRepository
        extends JpaRepository<
                UserRoleJpaEntity,
                UserRoleId
                > {

    boolean existsByIdUserIdAndIdRoleId(
            Long userId,
            Long roleId
    );
}
