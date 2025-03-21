package com.falesdev.blog.repository;

import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .name("ADMIN")
                .build();
        entityManager.persistAndFlush(role);

        user = User.builder()
                .email("fabricio@example.com")
                .password("securepass")
                .name("Fabricio Rodriguez")
                .roles(new HashSet<>(Set.of(role)))
                .build();
        entityManager.persistAndFlush(user);
    }

    @Test
    void saveUser() {
        Role role = Role.builder()
                .name("USER")
                .build();
        entityManager.persistAndFlush(role);

        User user = User.builder()
                .email("fabricio-1998-xd@hotmail.com")
                .password("securepass")
                .name("Fabricio Rodriguez")
                .roles(new HashSet<>(Set.of(role)))
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("fabricio-1998-xd@hotmail.com");
        assertThat(savedUser.getName()).isEqualTo("Fabricio Rodriguez");
        assertThat(savedUser.getRoles()).hasSize(1);
    }

    @Test
    void findById_UserExists() {
        Optional<User> foundUser = userRepository.findById(user.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("fabricio@example.com");
        assertThat(foundUser.get().getName()).isEqualTo("Fabricio Rodriguez");
    }

    @Test
    void findById_UserNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<User> foundUser = userRepository.findById(uuid);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void deleteUser() {
        UUID userId = user.getId();
        UUID roleId = user.getRoles().iterator().next().getId();
        assertThat(userRepository.existsById(userId)).isTrue();

        userRepository.deleteById(userId);
        entityManager.flush();

        assertThat(userRepository.findById(userId)).isEmpty();

        assertThat(entityManager.find(Role.class, roleId)).isNotNull();
    }

    @Test
    void findByEmail() {
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("fabricio@example.com");
    }

    @Test
    void existsByEmailIgnoreCase() {
        boolean exists1 = userRepository.existsByEmailIgnoreCase("FABRICIO@EXAMPLE.COM");
        boolean exists2 = userRepository.existsByEmailIgnoreCase("fabricio@example.com");
        boolean exists3 = userRepository.existsByEmailIgnoreCase("test@user.com");

        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isFalse();
    }
}
