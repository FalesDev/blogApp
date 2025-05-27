package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private Long jwtExpiryMs;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshExpiryMs;

    @Override
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature", e);
        }
    }

    @Override
    public long getExpirationTime(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    @Override
    public Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        BlogUserDetails blogUser = (BlogUserDetails) userDetails;

        Set<String> roleNames = blogUser.getUser().getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(blogUser.getUsername())
                .claim("userId", blogUser.getId())
                .claim("role", roleNames)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        BlogUserDetails collegeUser = (BlogUserDetails) userDetails;

        return Jwts.builder()
                .setSubject(collegeUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public long getJwtExpirationMs() {
        return jwtExpiryMs;
    }

    @Override
    public long getRefreshExpirationMs() {
        return refreshExpiryMs;
    }
}
