package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.application.exception.DuplicateEmailException;
import com.spsk1313.identityservice.identity.application.port.out.UserRepository;
import com.spsk1313.identityservice.identity.domain.EmailAddress;
import com.spsk1313.identityservice.identity.domain.User;
import com.spsk1313.identityservice.identity.infrastructure.persistence.adapter.JpaUserRepositoryAdapter;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import({
        JpaUserRepositoryAdapter.class,
        UserPersistenceMapper.class
})
public class JpaUserRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    private static final String EMAIL_STR = "sahil@example.com";
    private static final EmailAddress EMAIL = new EmailAddress(EMAIL_STR);
    private static final String PASSWORD_HASH = "$2a$12$R9h/cIPz0gi.Ns1KVptSMu7iUBvZovwAt6b7S27v.S3U7fT6yYpqu";

    @Test
    void shouldRejectDuplicateEmailAtDatabaseBoundary() {
        User user1 = User.register(EMAIL, PASSWORD_HASH);
        User user2 = User.register(EMAIL, PASSWORD_HASH);

        userRepository.save(user1);
        var ex = assertThrows(DuplicateEmailException.class, () -> userRepository.save(user2));
    }

    @Test
    void shouldUpdateExistingUserWithoutLosingCreatedAt() {
        User user = User.register(
                new EmailAddress("sahil@example.com"),
                PASSWORD_HASH
        );

        User persistedUser = userRepository.save(user);

        UserJpaEntity beforeUpdate = springDataUserRepository
                .findById(persistedUser.getId())
                .orElseThrow();

        Instant originalCreatedAt = beforeUpdate.getCreatedAt();

        persistedUser.verifyEmail();

        userRepository.save(persistedUser);

        UserJpaEntity afterUpdate = springDataUserRepository
                .findById(persistedUser.getId())
                .orElseThrow();

        assertTrue(afterUpdate.isEmailVerified());
        assertNotNull(afterUpdate.getCreatedAt());
        assertEquals(originalCreatedAt, afterUpdate.getCreatedAt());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = User.register(EMAIL, PASSWORD_HASH);

        User persistedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail(EMAIL);

        assertTrue(foundUser.isPresent());
        assertEquals(persistedUser.getId(), foundUser.get().getId());
        assertEquals(EMAIL, foundUser.get().getEmail());
        assertEquals(PASSWORD_HASH, foundUser.get().getPasswordHash());
    }

}
