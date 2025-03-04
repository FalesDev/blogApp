package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.AuthResponse;
import com.falesdev.blog.domain.dtos.requests.SignupRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    AuthResponse authenticate(String email, String password);
    AuthResponse register(SignupRequest signupRequest);
    String generateToken(UserDetails userDetails);
    UserDetails validateToken(String token);
}
