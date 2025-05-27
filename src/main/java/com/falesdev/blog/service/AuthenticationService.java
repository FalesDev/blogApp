package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.dto.response.AuthUserResponse;
import com.falesdev.blog.domain.dto.request.RegisterRequest;
import com.falesdev.blog.security.BlogUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public interface AuthenticationService {
    AuthResponse authenticate(String email, String password);
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse handleGoogleAuth(String code);
    UserDetails validateToken(String token);
    AuthUserResponse getUserProfile(BlogUserDetails userDetails);
}
