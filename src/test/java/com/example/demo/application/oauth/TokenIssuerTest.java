package com.example.demo.application.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TokenIssuerTest extends AbstractIntegrationTest {

    @Autowired
    private TokenIssuer sut;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void refreshToken이_없으면_새로_생성해서_저장한다() {
        // given
        Long userId = 1L;

        // when
        TokenResponse token = sut.issueTokens(userId);

        // then
        RefreshToken saved = refreshTokenRepository.findByUserId(userId).orElseThrow();

        assertThat(token.refreshToken()).isEqualTo(saved.getToken());
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    void refreshToken이_이미_있으면_rotate하여_갱신한다() {
        // given
        Long userId = 1L;
        RefreshToken existing = new RefreshToken(userId, "old-refresh");
        refreshTokenRepository.save(existing);

        // when
        TokenResponse token = sut.issueTokens(userId);

        // then
        RefreshToken updated = refreshTokenRepository.findByUserId(userId).orElseThrow();

        assertThat(updated.getToken()).isEqualTo(token.refreshToken());
        assertThat(updated.getToken()).isNotEqualTo("old-refresh");
    }

    @Test
    void reissueTokens는_새_토큰을_생성하고_refreshToken을_rotate_후_저장한다() {
        // given
        Long userId = 1L;
        RefreshToken saved = refreshTokenRepository.save(new RefreshToken(userId, "old-refresh"));

        // when
        TokenResponse response = sut.reissueTokens(saved);

        // then
        RefreshToken reloaded = refreshTokenRepository.findByUserId(userId).orElseThrow();

        assertThat(response.refreshToken()).isEqualTo(reloaded.getToken());
        assertThat(reloaded.getToken()).isNotEqualTo("old-refresh");
        assertThat(reloaded.getUserId()).isEqualTo(userId);
    }
}
