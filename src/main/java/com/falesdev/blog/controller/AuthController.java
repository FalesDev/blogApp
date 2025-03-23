package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.AuthResponse;
import com.falesdev.blog.domain.dto.AuthUser;
import com.falesdev.blog.domain.dto.request.LoginRequest;
import com.falesdev.blog.domain.dto.request.SignupRequest;
import com.falesdev.blog.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Controller for Authentication")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate a user",
            description = "Returns a JWT token and expiration time on successful authentication"
    )
    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

    @Operation(
            summary = "Get current user profile",
            description = "Returns details of the authenticated user"
    )
    @GetMapping("/me")
    public ResponseEntity<AuthUser> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(authenticationService.getUserProfile(authentication));
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns authentication details"
    )
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authenticationService.register(signupRequest));
    }
}
