package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.exception.DuplicateEmailException;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository userRepository;
    private final UserPersistenceMapper mapper;
    private static final String UNIQUE_EMAIL_CONSTRAINT = "uq_users_email";

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
        try {
            if (user.getId() == null) {
                UserJpaEntity entity = mapper.toEntity(user);
                entity = userRepository.save(entity);

                return mapper.toDomain(entity);
            }

            UserJpaEntity existingEntity = userRepository
                    .findById(user.getId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "User with id %d does not exist"
                                            .formatted(user.getId())
                            )
                    );

            existingEntity.updateFromDomain(
                    user.getEmail().value(),
                    user.getPasswordHash(),
                    user.isEmailVerified(),
                    user.getAccountStatus()
            );

            existingEntity = userRepository.save(existingEntity);

            return mapper.toDomain(existingEntity);

        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_EMAIL_CONSTRAINT.equals(
                    constraintViolation.getConstraintName()
            )) {
                throw new DuplicateEmailException();
            }

            throw ex;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return userRepository
                .findByEmail(email.value())
                .map(mapper::toDomain);
    }
}
