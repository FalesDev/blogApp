package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.dto.response.AuthUserResponse;
import com.falesdev.blog.domain.dto.request.LoginRequest;
import com.falesdev.blog.domain.dto.request.RegisterRequest;
import com.falesdev.blog.security.BlogUserDetails;
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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse expectedResponse;
    private AuthUserResponse expectedUser;
    private BlogUserDetails blogUserDetails;

    @BeforeEach
    public void init() {
        loginRequest = LoginRequest.builder().email("test@example.com").password("password").build();
        expectedResponse = AuthResponse.builder()
                .accessToken("jwt.token.here")
                .build();
        blogUserDetails = mock(BlogUserDetails.class);
        expectedUser = AuthUserResponse.builder()
                .email("user@example.com")
                .firstName("Test User")
                .lastName("Cyber")
                .build();
        registerRequest = RegisterRequest.builder()
                .firstName("Test User")
                .lastName("Cyber")
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
        when(authenticationService.getUserProfile(eq(blogUserDetails))).thenReturn(expectedUser);

        ResponseEntity<AuthUserResponse> response = authController.getUserProfile(blogUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedUser);

        verify(authenticationService).getUserProfile(eq(blogUserDetails));
    }

    @Test
    @DisplayName("Registration successful - Returns AuthResponse")
    void signup_ValidRequest_ReturnsAuthResponse() {
        when(authenticationService.register(eq(registerRequest))).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);

        verify(authenticationService).register(eq(registerRequest));
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
