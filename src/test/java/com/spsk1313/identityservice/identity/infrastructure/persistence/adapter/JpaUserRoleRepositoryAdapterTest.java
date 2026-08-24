package com.spsk1313.identityservice.identity.infrastructure.persistence.adapter;

import com.spsk1313.identityservice.identity.domain.authorization.RoleName;
import com.spsk1313.identityservice.identity.domain.authorization.UserRole;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.RoleJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataRoleRepository;
import com.spsk1313.identityservice.identity.infrastructure.persistence.repository.SpringDataUserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserRoleRepositoryAdapterTest {

    @Mock
    private SpringDataRoleRepository roleRepository;

    @Mock
    private SpringDataUserRoleRepository userRoleRepository;

    private JpaUserRoleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaUserRoleRepositoryAdapter(
                roleRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldSaveUserRole() {
        UserRole userRole =
                UserRole.assign(
                        8L,
                        RoleName.USER
                );

        RoleJpaEntity roleEntity =
                mock(RoleJpaEntity.class);

        when(roleEntity.getId())
                .thenReturn(3L);

        when(roleRepository.findByName(RoleName.USER))
                .thenReturn(Optional.of(roleEntity));

        UserRole result =
                adapter.save(userRole);

        ArgumentCaptor<UserRoleJpaEntity> captor =
                ArgumentCaptor.forClass(
                        UserRoleJpaEntity.class
                );

        verify(roleRepository)
                .findByName(RoleName.USER);

        verify(userRoleRepository)
                .save(captor.capture());

        UserRoleJpaEntity savedEntity =
                captor.getValue();

        assertEquals(
                8L,
                savedEntity.getId().getUserId()
        );

        assertEquals(
                3L,
                savedEntity.getId().getRoleId()
        );

        assertEquals(8L, result.getUserId());
        assertEquals(RoleName.USER, result.getRole());
    }

    @Test
    void shouldRejectSaveWhenRequiredRoleDoesNotExist() {
        UserRole userRole = UserRole.assign(
                8L,
                RoleName.USER
        );

        when(roleRepository.findByName(RoleName.USER))
                .thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> adapter.save(userRole)
                );

        assertEquals(
                "Required role does not exist: USER",
                exception.getMessage()
        );

        verify(roleRepository)
                .findByName(RoleName.USER);

        verifyNoInteractions(userRoleRepository);
    }

    @Test
    void shouldRejectNullUserRole() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> adapter.save(null)
                );

        assertEquals(
                "User role cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                roleRepository,
                userRoleRepository
        );
    }

    @Test
    void shouldReturnTrueWhenUserAlreadyHasRole() {
        RoleJpaEntity roleEntity =
                mock(RoleJpaEntity.class);

        when(roleRepository.findByName(
                RoleName.MODERATOR
        )).thenReturn(Optional.of(roleEntity));

        when(roleEntity.getId())
                .thenReturn(2L);

        when(userRoleRepository
                .existsByIdUserIdAndIdRoleId(
                        42L,
                        2L
                ))
                .thenReturn(true);

        boolean exists =
                adapter.existsByUserIdAndRole(
                        42L,
                        RoleName.MODERATOR
                );

        assertTrue(exists);

        verify(roleRepository)
                .findByName(RoleName.MODERATOR);

        verify(userRoleRepository)
                .existsByIdUserIdAndIdRoleId(
                        42L,
                        2L
                );
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        RoleJpaEntity roleEntity =
                mock(RoleJpaEntity.class);

        when(roleRepository.findByName(
                RoleName.MODERATOR
        )).thenReturn(Optional.of(roleEntity));

        when(roleEntity.getId())
                .thenReturn(2L);

        when(userRoleRepository
                .existsByIdUserIdAndIdRoleId(
                        42L,
                        2L
                ))
                .thenReturn(false);

        boolean exists =
                adapter.existsByUserIdAndRole(
                        42L,
                        RoleName.MODERATOR
                );

        assertFalse(exists);

        verify(roleRepository)
                .findByName(RoleName.MODERATOR);

        verify(userRoleRepository)
                .existsByIdUserIdAndIdRoleId(
                        42L,
                        2L
                );
    }

    @Test
    void shouldRejectExistsCheckWhenRequiredRoleDoesNotExist() {
        when(roleRepository.findByName(
                RoleName.MODERATOR
        )).thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> adapter.existsByUserIdAndRole(
                                42L,
                                RoleName.MODERATOR
                        )
                );

        assertEquals(
                "Required role does not exist: MODERATOR",
                exception.getMessage()
        );

        verify(roleRepository)
                .findByName(RoleName.MODERATOR);

        verifyNoInteractions(userRoleRepository);
    }
}