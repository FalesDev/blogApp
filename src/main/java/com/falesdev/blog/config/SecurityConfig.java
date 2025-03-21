package com.falesdev.blog.config;

import com.falesdev.blog.domain.dto.ApiErrorResponse;
import com.falesdev.blog.repository.UserRepository;
import com.falesdev.blog.security.BlogUserDetailsService;
import com.falesdev.blog.security.JwtAuthenticationFilter;
import com.falesdev.blog.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService){
        return new JwtAuthenticationFilter(authenticationService);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository){
        return new BlogUserDetailsService(userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.authorizeHttpRequests(auth ->auth
                    .requestMatchers(HttpMethod.POST,"/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/auth/signup").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v1/auth/me").authenticated()
                    .requestMatchers(HttpMethod.GET,"/api/v1/posts/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/posts").hasAnyRole("ADMIN","USER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").hasAnyRole("ADMIN","USER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").hasAnyRole("ADMIN","USER")
                    .requestMatchers(HttpMethod.GET,"/api/v1/posts/drafts").hasAnyRole("ADMIN","USER")
                    .requestMatchers(HttpMethod.GET,"/api/v1/categories/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,"/api/v1/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/api/v1/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/api/v1/tags/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/tags/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/api/v1/tags/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/api/v1/roles/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .csrf(csrf ->csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                );
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message("You don't have permission to access this resource")
                    .build();

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Authentication failed: Invalid authentication token")
                    .build();

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
}
