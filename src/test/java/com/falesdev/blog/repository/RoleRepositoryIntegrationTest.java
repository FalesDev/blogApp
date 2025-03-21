package com.falesdev.blog.repository;

import com.falesdev.blog.domain.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .name("ADMIN")
                .build();

        entityManager.persistAndFlush(role);
    }

    @Test
    void saveRole() {
        Role role = Role.builder()
                .name("USER")
                .build();

        Role savedRole = roleRepository.save(role);

        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("USER");
    }

    @Test
    void findById_RoleExists() {
        Optional<Role> foundRole = roleRepository.findById(role.getId());

        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    void findById_RoleNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<Role> foundRole = roleRepository.findById(uuid);

        assertThat(foundRole).isEmpty();
    }

    @Test
    void deleteRole() {
        UUID roleId = role.getId();
        assertThat(roleRepository.existsById(roleId)).isTrue();

        roleRepository.delete(role);
        entityManager.flush();

        assertThat(roleRepository.findById(roleId)).isEmpty();
    }

    @Test
    void findByName() {
        Optional<Role> foundRole = roleRepository.findByName(role.getName());

        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ADMIN");
    }
}
