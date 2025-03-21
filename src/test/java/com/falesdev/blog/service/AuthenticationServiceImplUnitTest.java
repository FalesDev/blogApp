package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.AuthResponse;
import com.falesdev.blog.domain.dto.AuthUser;
import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.dto.request.SignupRequest;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.exception.EmailAlreadyExistsException;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.service.impl.AuthenticationServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import com.falesdev.blog.domain.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "password";
    private final String testToken = "jwt.token";
    private final org.springframework.security.core.userdetails.User userDetails =
            new org.springframework.security.core.userdetails.User(
                    testEmail,
                    testPassword,
                    new ArrayList<>()
            );

    @Test
    @DisplayName("Authenticate user - Success")
    void authenticate_ValidCredentials_ReturnsToken() {
        when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(jwtService.generateToken(eq(userDetails))).thenReturn(testToken);
        when(jwtService.getExpirationTime(testToken)).thenReturn(3600L);

        AuthResponse response = authenticationService.authenticate(testEmail, testPassword);

        assertThat(response.getToken()).isEqualTo(testToken);
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword)
        );
        verify(userDetailsService).loadUserByUsername(testEmail);
    }

    @Test
    @DisplayName("Register new user - Success")
    void register_NewUser_ReturnsToken() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        Role userRole = Role.builder().name("USER").build();

        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(eq(testPassword))).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn(testToken);
        when(jwtService.getExpirationTime(testToken)).thenReturn(3600L);

        AuthResponse response = authenticationService.register(request);

        assertThat(response.getToken()).isEqualTo(testToken);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(testEmail) &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(userRole)
        ));
        verify(roleRepository, times(1)).findByName("USER");
        verify(passwordEncoder).encode(eq(testPassword));
    }

    @Test
    @DisplayName("Register with existing email - Throws Exception")
    void register_ExistingEmail_ThrowsException() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("Register with missing USER role - Throws Exception")
    void register_MissingUserRole_ThrowsException() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role USER not found");
    }

    @Test
    @DisplayName("Generate token - Success")
    void generateToken_ValidUser_ReturnsToken() {
        User userEntity = new User();
        userEntity.setEmail(testEmail);

        BlogUserDetails blogUserDetails = new BlogUserDetails(userEntity);

        when(jwtService.generateToken(eq(blogUserDetails))).thenReturn(testToken);

        String token = authenticationService.generateToken(blogUserDetails);

        verify(jwtService).generateToken(eq(blogUserDetails));
        assertThat(token).isEqualTo(testToken);
    }

    @Test
    @DisplayName("Validate valid token - Success")
    void validateToken_ValidToken_ReturnsUserDetails() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseClaims(eq(testToken))).thenReturn(claims);
        when(claims.getSubject()).thenReturn(testEmail);
        when(userDetailsService.loadUserByUsername(eq(testEmail))).thenReturn(userDetails);

        UserDetails result = authenticationService.validateToken(testToken);

        assertThat(result.getUsername()).isEqualTo(testEmail);
        verify(jwtService).parseClaims(eq(testToken));
        verify(userDetailsService).loadUserByUsername(eq(testEmail));
    }

    @Test
    @DisplayName("Validate invalid token - Throws Exception")
    void validateToken_InvalidToken_ThrowsException() {
        when(jwtService.parseClaims(anyString())).thenThrow(JwtException.class);

        assertThatThrownBy(() -> authenticationService.validateToken("invalid.token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication error");
    }

    @Test
    @DisplayName("Validate expired token - Throws Exception")
    void validateToken_ExpiredToken_ThrowsException() {
        when(jwtService.parseClaims(testToken)).thenThrow(ExpiredJwtException.class);

        assertThatThrownBy(() -> authenticationService.validateToken(testToken))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    @DisplayName("Get user profile - Success")
    void getUserProfile_ValidAuthentication_ReturnsAuthUser() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("USER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRoles(roles);

        BlogUserDetails userDetails = new BlogUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AuthUser authUser = authenticationService.getUserProfile(authentication);
        
        assertThat(authUser.getId()).isEqualTo(user.getId());
        assertThat(authUser.getName()).isEqualTo("Test User");
        assertThat(authUser.getEmail()).isEqualTo("test@example.com");
        Set<RoleDto> roleDtos = authUser.getRoles();
        assertThat(roleDtos.size()).isEqualTo(1);
        RoleDto roleDto = roleDtos.iterator().next();
        assertThat(roleDto.getId()).isEqualTo(role.getId());
        assertThat(roleDto.getName()).isEqualTo("USER");
        verify(authentication).getPrincipal();
    }
}
