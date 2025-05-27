package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.entity.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId);
    AuthResponse refreshAccessToken(String refreshToken);
    void validateRefreshToken(String refreshToken);
}
