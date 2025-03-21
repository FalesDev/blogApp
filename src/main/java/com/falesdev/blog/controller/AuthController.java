package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.AuthResponse;
import com.falesdev.blog.domain.dto.AuthUser;
import com.falesdev.blog.domain.dto.request.LoginRequest;
import com.falesdev.blog.domain.dto.request.SignupRequest;
import com.falesdev.blog.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(authenticationService.getUserProfile(authentication));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authenticationService.register(signupRequest));
    }
}
