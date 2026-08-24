package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.port.out.UserRoleRepository;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RoleJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataRoleRepository;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserRoleRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaUserRoleRepositoryAdapter
        implements UserRoleRepository {

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataUserRoleRepository userRoleRepository;

    public JpaUserRoleRepositoryAdapter(
            SpringDataRoleRepository roleRepository,
            SpringDataUserRoleRepository userRoleRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserRole save(UserRole userRole) {

        if (userRole == null) {
            throw new IllegalArgumentException(
                    "User role cannot be null"
            );
        }

        RoleJpaEntity roleEntity = roleRepository
                .findByName(userRole.getRole())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Required role does not exist: "
                                        + userRole.getRole()
                        )
                );

        UserRoleJpaEntity entity =
                new UserRoleJpaEntity(
                        userRole.getUserId(),
                        roleEntity.getId()
                );

        userRoleRepository.save(entity);

        return UserRole.reconstitute(
                userRole.getUserId(),
                userRole.getRole()
        );
    }

    @Override
    public boolean existsByUserIdAndRole(Long userId, RoleName role) {
        RoleJpaEntity roleEntity = roleRepository
                .findByName(role)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Required role does not exist: "
                                        + role
                        )
                );

        return userRoleRepository
                .existsByIdUserIdAndIdRoleId(userId, roleEntity.getId()
                );
    }
}