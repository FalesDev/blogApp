package com.falesdev.blog.controllers;

import com.falesdev.blog.domain.dtos.AuthResponse;
import com.falesdev.blog.domain.dtos.AuthUser;
import com.falesdev.blog.domain.dtos.requests.LoginRequest;
import com.falesdev.blog.domain.dtos.requests.SignupRequest;
import com.falesdev.blog.domain.entities.User;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
        BlogUserDetails userDetails = (BlogUserDetails) authentication.getPrincipal();
        AuthUser response = new AuthUser(
                userDetails.getId(),
                userDetails.getUser().getName(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authenticationService.register(signupRequest));
    }
}
