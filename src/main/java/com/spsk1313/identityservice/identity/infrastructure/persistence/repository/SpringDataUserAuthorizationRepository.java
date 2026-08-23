package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleId;
import com.spsk1313.identityservice.identity.infrastructure.persistence.projection.UserAuthorizationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataUserAuthorizationRepository
        extends JpaRepository<UserRoleJpaEntity, UserRoleId> {

    @Query(
            value = """
                    SELECT
                        r.name AS roleName,
                        p.name AS permissionName
                    FROM user_roles ur
                    JOIN roles r
                        ON r.id = ur.role_id
                    LEFT JOIN role_permissions rp
                        ON rp.role_id = r.id
                    LEFT JOIN permissions p
                        ON p.id = rp.permission_id
                    WHERE ur.user_id = :userId
                    """,
            nativeQuery = true
    )
    List<UserAuthorizationProjection> findAuthorizationByUserId(
            @Param("userId") Long userId
    );
}