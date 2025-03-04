package com.falesdev.blog.services;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;

public interface JwtService {

    String generateToken(UserDetails userDetails);
    Claims parseClaims(String token);
    long getExpirationTime(String token);
    Key getSigningKey();
}
