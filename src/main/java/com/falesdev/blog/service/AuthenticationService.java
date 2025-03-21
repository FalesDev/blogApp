package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.AuthResponse;
import com.falesdev.blog.domain.dto.AuthUser;
import com.falesdev.blog.domain.dto.request.SignupRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    AuthResponse authenticate(String email, String password);
    AuthResponse register(SignupRequest signupRequest);
    String generateToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(Authentication authentication);
}
