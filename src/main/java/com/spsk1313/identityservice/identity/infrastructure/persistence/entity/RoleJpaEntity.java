package com.spsk1313.identityservice.identity.infrastructure.persistence.entity;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleName name;

    protected RoleJpaEntity() {
    }

    public RoleJpaEntity(RoleName name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public RoleName getName() {
        return name;
    }
}