package com.falesdev.blog.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleAuthConfig {

    @Value("${google.client.web.id}")
    private String webClientId;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        if (webClientId == null || webClientId.isBlank()) {
            throw new IllegalStateException("Google Web Client ID not configured");
        }

        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(webClientId))
                .build();
    }
}
