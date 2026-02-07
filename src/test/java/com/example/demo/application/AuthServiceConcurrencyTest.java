package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.util.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthServiceConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService sut;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 동일한_리프레시_토큰으로_동시_재발급_시_모두_성공하지만_마지막_토큰만_유효하다() throws InterruptedException {
        // Given
        Long userId = 1L;
        TokenResponse initialToken = tokenProvider.generateToken(userId);
        RefreshToken refreshToken = new RefreshToken(userId, initialToken.refreshToken());
        refreshTokenRepository.save(refreshToken);
        entityManager.clear();

        // When: 동시 재발급
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<TokenResponse> responses = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    TokenResponse response = sut.reissueToken(initialToken.refreshToken());
                    responses.add(response);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then: 모두 응답받음
        assertThat(responses).hasSize(threadCount);

        // DB에 저장된 토큰 확인
        RefreshToken saved = refreshTokenRepository.findByUserId(userId).orElseThrow();

        // 응답받은 토큰 중 하나만 DB에 있음 (마지막에 저장된 것)
        long validTokenCount = responses.stream()
            .filter(response -> saved.isSameToken(response.refreshToken()))
            .count();

        assertThat(validTokenCount).isEqualTo(1);
    }
}
