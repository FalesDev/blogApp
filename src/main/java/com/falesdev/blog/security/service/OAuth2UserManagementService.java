package com.falesdev.blog.security.service;

import com.falesdev.blog.domain.RegisterType;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.exception.AuthenticationMethodConflictException;
import com.falesdev.blog.exception.BadRequestException;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuth2UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createOrUpdateUserFromGoogle(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String picture = (String) payload.get("picture");
        return createOrUpdateOAuth2User(email, firstName, lastName, picture, "GOOGLE");
    }

    public User createOrUpdateOAuth2User(
            String email, String firstName, String lastName, String picture, String provider) {

        RegisterType registerType;
        try {
            registerType = RegisterType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.getRegisterType() != registerType) {
                        throw new AuthenticationMethodConflictException("Account registered with "
                                + existingUser.getRegisterType());
                    }
                    existingUser.setFirstName(firstName);
                    existingUser.setLastName(lastName);
                    existingUser.setImageURL(picture);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .password(null)
                                .firstName(firstName)
                                .lastName(lastName)
                                .roles(new HashSet<>(Set.of(defaultRole)))
                                .imageURL(picture)
                                .registerType(registerType)
                                .build()
                ));
    }
}
