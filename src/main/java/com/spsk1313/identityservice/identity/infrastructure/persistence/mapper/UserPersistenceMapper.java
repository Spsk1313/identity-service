package com.spsk1313.identityservice.identity.infrastructure.persistence.mapper;

import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        Objects.requireNonNull(user, "User domain object cannot be null");
        return new UserJpaEntity(
                user.getId(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isEmailVerified(),
                user.getAccountStatus()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        Objects.requireNonNull(entity, "User entity cannot be null");

        EmailAddress email = new EmailAddress(entity.getEmail());

        return User.reconstitute(
                entity.getId(),
                email,
                entity.getPasswordHash(),
                entity.isEmailVerified(),
                entity.getAccountStatus()
        );
    }
}
