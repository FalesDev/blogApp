package com.falesdev.blog.services.impl;

import com.falesdev.blog.domain.dtos.AuthResponse;
import com.falesdev.blog.domain.dtos.AuthUser;
import com.falesdev.blog.domain.dtos.RoleDto;
import com.falesdev.blog.domain.dtos.requests.SignupRequest;
import com.falesdev.blog.domain.entities.Role;
import com.falesdev.blog.domain.entities.User;
import com.falesdev.blog.exceptions.EmailAlreadyExistsException;
import com.falesdev.blog.respositories.RoleRepository;
import com.falesdev.blog.respositories.UserRepository;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.services.AuthenticationService;
import com.falesdev.blog.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String token = generateToken(userDetails);
        long expiresIn = jwtService.getExpirationTime(token);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public AuthResponse register(SignupRequest signupRequest) {
        if (userRepository.existsByEmailIgnoreCase(signupRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

        User newUser = User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .roles(new HashSet<>())
                .build();

        newUser.getRoles().add(userRole);
        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getEmail());
        String token = jwtService.generateToken(userDetails);
        long expiresIn = jwtService.getExpirationTime(token);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }


    @Override
    public String generateToken(UserDetails userDetails) {
        return jwtService.generateToken(userDetails);
    }

    @Override
    public UserDetails validateToken(String token) {
        try {
            final Claims claims = jwtService.parseClaims(token);
            final String username = claims.getSubject();

            return userDetailsService.loadUserByUsername(username);
        } catch (ExpiredJwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired", ex);
        } catch (JwtException | UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication error", ex);
        }
    }

    @Override
    public AuthUser getUserProfile(Authentication authentication) {
        BlogUserDetails userDetails = (BlogUserDetails) authentication.getPrincipal();

        Set<RoleDto> roles = userDetails.getUser().getRoles().stream()
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        return new AuthUser(
                userDetails.getId(),
                userDetails.getUser().getName(),
                userDetails.getUsername(),
                roles
        );
    }
}
