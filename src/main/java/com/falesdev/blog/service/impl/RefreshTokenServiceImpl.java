package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.entity.RefreshToken;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.exception.InvalidRefreshTokenException;
import com.falesdev.blog.repository.RefreshTokenRepository;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.service.JwtService;
import com.falesdev.blog.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        refreshTokenRepository.deleteExpiredOrRevokedByUser(userId, Instant.now());

        User user = userRepository.findById(userId).orElseThrow();
        UserDetails userDetails = new BlogUserDetails(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(jwtService.generateRefreshToken(userDetails))
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);

                    User user = userRepository.findById(token.getUserId())
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                    UserDetails userDetails = new BlogUserDetails(user);
                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                    RefreshToken newToken = RefreshToken.builder()
                            .userId(user.getId())
                            .token(newRefreshToken)
                            .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                            .revoked(false)
                            .build();
                    refreshTokenRepository.save(newToken);

                    long expiresIn = jwtService.getJwtExpirationMs() / 1000;

                    return AuthResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken)
                            .expiresIn(expiresIn)
                            .build();
                })
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
    }

    @Override
    @Transactional
    public void validateRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }
}
