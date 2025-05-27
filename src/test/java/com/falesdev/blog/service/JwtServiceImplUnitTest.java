package com.falesdev.blog.service;

import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class JwtServiceImplUnitTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final long jwtExpiryMs  = 3600000; // 1 hora
    private final long refreshExpiryMs = 86400000; // 24 horas para refresh token
    private BlogUserDetails userDetails;

    @BeforeEach
    void setUp() {
        String secretKey = "mySecretKey12345678901234567890123456789012";
        ReflectionTestUtils.setField(jwtService, "secretKey", Base64.getEncoder().encodeToString(secretKey.getBytes()));
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", jwtExpiryMs);
        ReflectionTestUtils.setField(jwtService, "refreshExpiryMs", refreshExpiryMs);

        Role userRole = new Role();
        userRole.setName("USER");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("password")
                .roles(Set.of(userRole))
                .build();
        userDetails = new BlogUserDetails(user);
    }

    @Test
    @DisplayName("Generate token - Success")
    void generateToken_ValidUserDetails_ReturnsValidToken() {
        // Act
        String token = jwtService.generateAccessToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length == 3).isTrue();
    }

    @Test
    @DisplayName("Generate Refresh Token - Success")
    void generateRefreshToken_ValidUserDetails_ReturnsValidToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Parse claims - Success")
    void parseClaims_ValidToken_ReturnsClaims() {
        String token = jwtService.generateAccessToken(userDetails);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.getExpiration().after(new Date())).isTrue();
    }

    @Test
    @DisplayName("Parse claims - Invalid Token Throws Exception")
    void parseClaims_InvalidToken_ThrowsJwtException() {
        String invalidToken = "invalid.token";

        assertThrows(JwtException.class, () -> jwtService.parseClaims(invalidToken));
    }

    @Test
    @DisplayName("Parse claims - Expired Token Throws Exception")
    void parseClaims_ExpiredToken_ThrowsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", -1000L);
        String expiredToken = jwtService.generateAccessToken(userDetails);

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseClaims(expiredToken));
    }

    @Test
    @DisplayName("Parse claims - Tampered Token Throws Exception")
    void parseClaims_TamperedToken_ThrowsSignatureException() {
        String validToken = jwtService.generateAccessToken(userDetails);
        String tamperedToken = validToken + "tampered";

        JwtException exception = assertThrows(
                JwtException.class,
                () -> jwtService.parseClaims(tamperedToken)
        );

        assertThat(exception.getCause()).isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Get Expiration Time - Access Token")
    void getExpirationTime_AccessToken_ReturnsCorrectRemainingTime() {
        String token = jwtService.generateAccessToken(userDetails);
        long expirationTime = jwtService.getExpirationTime(token);

        assertTrue(expirationTime > 0 && expirationTime <= jwtExpiryMs);
    }

    @Test
    @DisplayName("Get Expiration Time - Refresh Token")
    void getExpirationTime_RefreshToken_ReturnsCorrectRemainingTime() {
        String token = jwtService.generateRefreshToken(userDetails);
        long expirationTime = jwtService.getExpirationTime(token);

        assertTrue(expirationTime > 0 && expirationTime <= refreshExpiryMs);
    }

    @Test
    @DisplayName("Signature Key - Correct Algorithm")
    void getSigningKey_ValidSecret_ReturnsCorrectKey() {
        Key key = jwtService.getSigningKey();
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    @DisplayName("Generate Access Token - Custom Expiration")
    void generateAccessToken_WithCustomExpiration_HasCorrectExpiration() {
        long customExpiration = 5000L;
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", customExpiration);

        String token = jwtService.generateAccessToken(userDetails);
        Claims claims = jwtService.parseClaims(token);

        long expectedExpiration = claims.getIssuedAt().getTime() + customExpiration;
        assertThat(claims.getExpiration().getTime()).isEqualTo(expectedExpiration);
    }

    @Test
    @DisplayName("Generate Refresh Token - Custom Expiration")
    void generateRefreshToken_WithCustomExpiration_HasCorrectExpiration() {
        long customExpiration = 10000L;
        ReflectionTestUtils.setField(jwtService, "refreshExpiryMs", customExpiration);

        String token = jwtService.generateRefreshToken(userDetails);
        Claims claims = jwtService.parseClaims(token);

        long expectedExpiration = claims.getIssuedAt().getTime() + customExpiration;
        assertThat(claims.getExpiration().getTime()).isEqualTo(expectedExpiration);
    }
}
