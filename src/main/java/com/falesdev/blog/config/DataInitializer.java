package com.falesdev.blog.config;

import com.falesdev.blog.domain.RegisterType;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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

            createUserIfNotFound(
                    "admin@test.com",
                    "Admin User",
                    "adminpassword",
                    adminRoles);
            createUserIfNotFound(
                    "user@test.com",
                    "Test User",
                    "password",
                    userRoles);
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

    private void createUserIfNotFound(String email, String firstName,
                                      String rawPassword, Set<Role> roles) {
        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating user: {}", email);
            return userRepository.save(
                    User.builder()
                            .email(email)
                            .firstName(firstName)
                            .lastName("Cyber")
                            .password(passwordEncoder.encode(rawPassword))
                            .roles(new HashSet<>(roles))
                            .registerType(RegisterType.LOCAL)
                            .build()
            );
        });
    }
}
