package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.request.GoogleRequest;
import com.falesdev.blog.domain.dto.request.RefreshTokenRequest;
import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.dto.response.AuthUserResponse;
import com.falesdev.blog.domain.dto.request.LoginRequest;
import com.falesdev.blog.domain.dto.request.RegisterRequest;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.service.AuthenticationService;
import com.falesdev.blog.service.JwtService;
import com.falesdev.blog.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Controller for Authentication")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Operation(
            summary = "Authenticate a user",
            description = "Returns a JWT token, Refresh Token and expiration time on successful authentication"
    )
    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns authentication details"
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @Operation(
            summary = "Login/Register a new google user",
            description = "Authenticates a user using a Google ID token. " +
                    "If the user doesn't exist, a new account is created."
    )
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @Valid @RequestBody GoogleRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.handleGoogleAuth(request.code())
        );
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token. " +
                    "The refresh token must be provided in the request body"
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.refreshToken()));
    }

    @Operation(
            summary = "Get current user profile",
            description = "Returns details of the authenticated user"
    )
    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> getUserProfile(
            @AuthenticationPrincipal BlogUserDetails userDetails
    ) {
        return ResponseEntity.ok(authenticationService.getUserProfile(userDetails));
    }
}
