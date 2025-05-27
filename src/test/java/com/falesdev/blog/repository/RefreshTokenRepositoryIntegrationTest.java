package com.falesdev.blog.repository;

import com.falesdev.blog.domain.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class RefreshTokenRepositoryIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        refreshToken = RefreshToken.builder()
                .token("refresh.token")
                .userId(userId)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        entityManager.persistAndFlush(refreshToken);
    }

    @Test
    void saveRefreshToken() {
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        assertThat(savedRefreshToken.getId()).isNotNull();
        assertThat(savedRefreshToken.getToken()).isEqualTo("refresh.token");
    }

    @Test
    void findById_RefreshTokenExists() {
        Optional<RefreshToken> foundRefreshToken = refreshTokenRepository.findById(refreshToken.getId());

        assertThat(foundRefreshToken).isPresent();
        assertThat(foundRefreshToken.get().getToken()).isEqualTo("refresh.token");
    }

    @Test
    void findById_RefreshTokenNotExists() {
        UUID uuid = UUID.randomUUID();
        Optional<RefreshToken> foundRefreshToken = refreshTokenRepository.findById(uuid);

        assertThat(foundRefreshToken).isEmpty();
    }

    @Test
    void findByToken() {
        Optional<RefreshToken> foundRefreshToken = refreshTokenRepository.findByToken(refreshToken.getToken());

        assertThat(foundRefreshToken).isPresent();
        assertThat(foundRefreshToken.get().getToken()).isEqualTo("refresh.token");
    }

    @Test
    void deleteRefreshToken() {
        UUID refreshTokenId = refreshToken.getId();
        assertThat(refreshTokenRepository.existsById(refreshTokenId)).isTrue();

        refreshTokenRepository.delete(refreshToken);
        entityManager.flush();

        assertThat(refreshTokenRepository.findById(refreshTokenId)).isEmpty();
    }
}
