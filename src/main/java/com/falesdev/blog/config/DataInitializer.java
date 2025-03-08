package com.falesdev.blog.config;

import com.falesdev.blog.domain.entities.Role;
import com.falesdev.blog.domain.entities.User;
import com.falesdev.blog.respositories.RoleRepository;
import com.falesdev.blog.respositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initializeData() {
        return args -> {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(createRoleIfNotFound("ADMIN"));

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(createRoleIfNotFound("USER"));

            createUserIfNotFound("admin@test.com", "Admin User", "adminpassword", adminRoles);
            createUserIfNotFound("user@test.com", "Test User", "password", userRoles);
        };
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating rol: {}", name);
                    return roleRepository.save(
                            Role.builder()
                                    .name(name)
                                    .build()
                    );
                });
    }

    private void createUserIfNotFound(String email, String name,
                                      String rawPassword, Set<Role> roles) {
        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating user: {}", email);
            return userRepository.save(
                    User.builder()
                            .email(email)
                            .name(name)
                            .password(passwordEncoder.encode(rawPassword))
                            .roles(new HashSet<>(roles))
                            .build()
            );
        });
    }
}
