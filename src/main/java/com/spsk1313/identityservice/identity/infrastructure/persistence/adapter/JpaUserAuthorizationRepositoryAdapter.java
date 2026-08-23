package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.port.out.UserAuthorizationRepository;
import com.spsk1313.identityservice.identity.domain.authorization.PermissionName;
import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserAuthorization;
import com.spsk1313.identityservice.identity.infrastructure.persistence.projection.UserAuthorizationProjection;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserAuthorizationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JpaUserAuthorizationRepositoryAdapter
        implements UserAuthorizationRepository {

    private final SpringDataUserAuthorizationRepository repository;

    public JpaUserAuthorizationRepositoryAdapter(
            SpringDataUserAuthorizationRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public UserAuthorization findByUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User id must be positive"
            );
        }

        List<UserAuthorizationProjection> rows =
                repository.findAuthorizationByUserId(userId);

        Set<RoleName> roles = rows.stream()
                .map(UserAuthorizationProjection::getRoleName)
                .filter(Objects::nonNull)
                .map(RoleName::valueOf)
                .collect(Collectors.toSet());

        Set<PermissionName> permissions = rows.stream()
                .map(UserAuthorizationProjection::getPermissionName)
                .filter(Objects::nonNull)
                .map(PermissionName::valueOf)
                .collect(Collectors.toSet());

        return UserAuthorization.of(
                userId,
                roles,
                permissions
        );
    }
}
