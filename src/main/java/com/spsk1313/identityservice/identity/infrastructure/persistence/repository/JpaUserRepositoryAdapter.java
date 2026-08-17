package com.spsk1313.identityservice.identity.infrastructure.persistence.repository;

import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository userRepository;
    private final UserPersistenceMapper mapper;

    public JpaUserRepositoryAdapter(SpringDataUserRepository userRepository, UserPersistenceMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return userRepository.existsByEmail(email.value());
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        entity = userRepository.save(entity);
        return mapper.toDomain(entity);
    }
}
