package com.spsk1313.identityservice.identity.infrastructure.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRoleJpaEntity {

    @EmbeddedId
    private UserRoleId id;

    protected UserRoleJpaEntity() {
    }

    public UserRoleJpaEntity(
            Long userId,
            Long roleId
    ) {
        this.id = new UserRoleId(
                userId,
                roleId
        );
    }
    public static UserRoleJpaEntity create(
            Long userId,
            Long roleId
    ) {
        return new UserRoleJpaEntity(userId, roleId);
    }

    public UserRoleId getId() {
        return id;
    }
}
