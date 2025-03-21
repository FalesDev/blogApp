package com.falesdev.blog.service;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplUnitTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String secretKey = "mySecretKey12345678901234567890123456789012";
    private final long expirationMs = 3600000; // 1 hora
    private final UserDetails userDetails = mock(UserDetails.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", Base64.getEncoder().encodeToString(secretKey.getBytes()));
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", expirationMs);

        when(userDetails.getUsername()).thenReturn("test@example.com");
    }

    @Test
    @DisplayName("Generate token - Success")
    void generateToken_ValidUserDetails_ReturnsValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length == 3).isTrue();
    }

    @Test
    @DisplayName("Parse claims - Success")
    void parseClaims_ValidToken_ReturnsClaims() {
        String token = jwtService.generateToken(userDetails);

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
        String expiredToken = jwtService.generateToken(userDetails);

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseClaims(expiredToken));
    }

    @Test
    @DisplayName("Parse claims - Tampered Token Throws Exception")
    void parseClaims_TamperedToken_ThrowsSignatureException() {
        String validToken = jwtService.generateToken(userDetails);
        String tamperedToken = validToken + "tampered";

        JwtException exception = assertThrows(
                JwtException.class,
                () -> jwtService.parseClaims(tamperedToken)
        );

        assertThat(exception.getCause()).isInstanceOf(SignatureException.class);
        assertThat(exception.getMessage()).contains("Invalid JWT signature");
    }

    @Test
    @DisplayName("Parse claims - Valid Token")
    void getExpirationTime_ValidToken_ReturnsCorrectRemainingTime() {
        String token = jwtService.generateToken(userDetails);

        long expirationTime = jwtService.getExpirationTime(token);

        assertTrue(expirationTime > 0 && expirationTime <= expirationMs);
    }

    @Test
    @DisplayName("Get expiration time - Precision Check")
    void getExpirationTime_ValidToken_ReturnsPreciseRemainingTime() {
        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.parseClaims(token);
        long issuedAtTime = claims.getIssuedAt().getTime();

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - issuedAtTime;

        long expectedExpirationMs = expirationMs - elapsedTime;
        long expirationTime = jwtService.getExpirationTime(token);

        assertThat(expirationTime).isCloseTo(expectedExpirationMs, within(500L));
    }

    @Test
    @DisplayName("Get signing key - Correct Algorithm")
    void getSigningKey_ValidSecret_ReturnsCorrectKey() {
        Key key = jwtService.getSigningKey();
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    @DisplayName("Generate token - With Custom Expiration Success")
    void generateToken_WithCustomExpiration_HasCorrectExpiration() {
        long customExpiration = 5000L;
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", customExpiration);

        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.parseClaims(token);

        long expectedExpiration = claims.getIssuedAt().getTime() + customExpiration;
        assertEquals(expectedExpiration, claims.getExpiration().getTime());
    }
}
