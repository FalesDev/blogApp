package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.AuthResponse;
import com.falesdev.blog.domain.dto.AuthUser;
import com.falesdev.blog.domain.dto.request.LoginRequest;
import com.falesdev.blog.domain.dto.request.SignupRequest;
import com.falesdev.blog.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private AuthResponse expectedResponse;
    private AuthUser expectedUser;
    private Authentication authentication;

    @BeforeEach
    public void init() {
        loginRequest = LoginRequest.builder().email("test@example.com").password("password").build();
        expectedResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .build();
        authentication = mock(Authentication.class);
        expectedUser = AuthUser.builder()
                .email("user@example.com")
                .name("Test User")
                .build();
        signupRequest = SignupRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();
    }

    @Test
    @DisplayName("Login successful - Returns AuthResponse")
    void login_ValidCredentials_ReturnsAuthResponse() {
        when(authenticationService.authenticate(
                eq(loginRequest.getEmail()),
                eq(loginRequest.getPassword())
        )).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);

        verify(authenticationService).authenticate(eq(loginRequest.getEmail()), eq(loginRequest.getPassword()));
    }

    @Test
    @DisplayName("Get authenticated user profile - Returns AuthUser")
    void getUserProfile_ValidAuthentication_ReturnsAuthUser() {
        when(authenticationService.getUserProfile(eq(authentication))).thenReturn(expectedUser);

        ResponseEntity<AuthUser> response = authController.getUserProfile(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedUser);

        verify(authenticationService).getUserProfile(eq(authentication));
    }

    @Test
    @DisplayName("Registration successful - Returns AuthResponse")
    void signup_ValidRequest_ReturnsAuthResponse() {
        when(authenticationService.register(eq(signupRequest))).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.signup(signupRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);

        verify(authenticationService).register(eq(signupRequest));
    }

    @Test
    @DisplayName("Login failed - Throws exception")
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationService.authenticate(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                authController.login(loginRequest)
        );

        assertThat(exception.getMessage()).isEqualTo("Invalid email or password");

        verify(authenticationService).authenticate(anyString(), anyString());
    }
}
