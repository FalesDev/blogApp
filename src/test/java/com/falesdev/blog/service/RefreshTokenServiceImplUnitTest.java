package com.falesdev.blog.service;


import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.entity.RefreshToken;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.exception.InvalidRefreshTokenException;
import com.falesdev.blog.repository.RefreshTokenRepository;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplUnitTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private final UUID userId = UUID.randomUUID();
    private final String refreshToken = "refresh.token";
    private final User user = User.builder().id(userId).email("test@example.com").build();

    @Test
    @DisplayName("Create refresh token - Delete existing tokens and create new ones")
    void createRefreshToken_DeletesExistingAndCreatesNew() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateRefreshToken(any())).thenReturn(refreshToken);
        when(jwtService.getRefreshExpirationMs()).thenReturn(86400000L);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            return RefreshToken.builder()
                    .userId(token.getUserId())
                    .token(token.getToken())
                    .expiryDate(token.getExpiryDate())
                    .revoked(token.isRevoked())
                    .build();
        });

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(refreshToken);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("RefreshAccessToken - Invalid token throws exception")
    void refreshAccessToken_InvalidToken_ThrowsException() {
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token not found");
    }

    @Test
    @DisplayName("RefreshAccessToken - Valid token generates new tokens")
    void refreshAccessToken_ValidToken_GeneratesNewTokens() {
        RefreshToken oldToken = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        String accessToken = "new.access.token";

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(oldToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(any())).thenReturn(refreshToken);
        when(jwtService.getJwtExpirationMs()).thenReturn(3600000L);

        AuthResponse response = refreshTokenService.refreshAccessToken(refreshToken);

        verify(refreshTokenRepository, times(1)).delete(oldToken);
        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));

        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Validate refresh token - Expired token throws exception")
    void validateRefreshToken_ExpiredToken_ThrowsException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .expiryDate(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token expired");
    }

    @Test
    @DisplayName("Validate refresh token - Revoked token throws exception")
    void validateRefreshToken_RevokedToken_ThrowsException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token revoked");
    }

    @Test
    @DisplayName("Validate refresh token - Valid token passes validation")
    void validateRefreshToken_ValidToken_NoExceptions() {
        RefreshToken validToken = RefreshToken.builder()
                .userId(userId)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(validToken));

        assertThatCode(() -> refreshTokenService.validateRefreshToken(refreshToken))
                .doesNotThrowAnyException();
    }
}
