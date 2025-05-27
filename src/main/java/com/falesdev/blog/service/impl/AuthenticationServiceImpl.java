package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.RegisterType;
import com.falesdev.blog.domain.dto.response.AuthResponse;
import com.falesdev.blog.domain.dto.response.AuthUserResponse;
import com.falesdev.blog.domain.dto.RoleDto;
import com.falesdev.blog.domain.dto.request.RegisterRequest;
import com.falesdev.blog.domain.entity.Role;
import com.falesdev.blog.domain.entity.User;
import com.falesdev.blog.exception.AuthenticationException;
import com.falesdev.blog.exception.EmailAlreadyExistsException;
import com.falesdev.blog.exception.ExternalServiceException;
import com.falesdev.blog.mapper.RoleMapper;
import com.falesdev.blog.repository.RoleRepository;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.security.BlogUserDetails;
import com.falesdev.blog.security.service.OAuth2UserManagementService;
import com.falesdev.blog.service.AuthenticationService;
import com.falesdev.blog.service.EmailService;
import com.falesdev.blog.service.JwtService;
import com.falesdev.blog.service.RefreshTokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final RoleMapper roleMapper;
    private final OAuth2UserManagementService oAuth2UserManagementService;
    private final GoogleIdTokenVerifier verifier;

    @Value("${google.client.web.id}")
    private String clientId;

    @Value("${google.client.web.secret}")
    private String clientSecret;

    @Override
    @Transactional
    public AuthResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );

        BlogUserDetails userDetails = (BlogUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId()).getToken();
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

        User newUser = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .roles(new HashSet<>())
                .imageURL(null)
                .registerType(RegisterType.LOCAL)
                .build();

        newUser.getRoles().add(userRole);
        userRepository.save(newUser);

        emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFirstName());
        return generateAuthResponse(newUser);
    }

    @Override
    @Transactional
    public AuthResponse handleGoogleAuth(String code) {
        GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);
        GoogleIdToken.Payload payload = validateIdToken(tokenResponse.getIdToken());
        User user = oAuth2UserManagementService.createOrUpdateUserFromGoogle(payload);
        return generateAuthResponse(user);
    }

    @Override
    public UserDetails validateToken(String token) {
        try {
            final Claims claims = jwtService.parseClaims(token);
            final String username = claims.getSubject();

            return userDetailsService.loadUserByUsername(username);
        } catch (ExpiredJwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired", ex);
        } catch (JwtException | UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication error", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse getUserProfile(BlogUserDetails userDetails) {
        User user = userDetails.getUser();

        Set<RoleDto> roles = user.getRoles().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toSet());

        return new AuthUserResponse(
                userDetails.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roles,
                user.getImageURL()
        );
    }

    private GoogleTokenResponse exchangeCodeForTokens(String code) {
        try {
            return new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    code,
                    "postmessage"
            ).execute();
        } catch (IOException e) {
            throw new ExternalServiceException("Error exchanging code", e);
        }
    }

    private GoogleIdToken.Payload validateIdToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) throw new AuthenticationException("Invalid token");

            GoogleIdToken.Payload payload = idTokenObj.getPayload();

            if (!payload.getIssuer().equals("https://accounts.google.com")
                    && !payload.getIssuer().equals("accounts.google.com")) {
                throw new AuthenticationException("Invalid token issuer");
            }
            return payload;
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthenticationException("Token validation failed", e);
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        BlogUserDetails userDetails = new BlogUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
