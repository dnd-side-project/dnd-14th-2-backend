package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.Provider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.application.exception.UnauthorizedException;
import com.example.demo.infrastructure.oauth.token.JwtProvider;
import com.example.demo.util.AbstractIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Value("${jwt.secret.key}")
    String secretKey;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 유효한_리프레쉬_토큰으로_새_토큰을_반환한다() {
        // given
        Long userId = 1L;
        TokenResponse initialToken = tokenProvider.generateToken(userId);
        refreshTokenRepository.save(new RefreshToken(userId, initialToken.refreshToken()));

        // when
        TokenResponse result = sut.reissueToken(initialToken.refreshToken());

        // then
        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();
        assertThat(result.accessToken()).isNotEqualTo(initialToken.accessToken());
        assertThat(result.refreshToken()).isNotEqualTo(initialToken.refreshToken());
    }

    @Test
    void 토큰_재발급_시_저장된_리프레쉬_토큰이_변경된다() {
        // given
        Long userId = 1L;
        TokenResponse initialToken = tokenProvider.generateToken(userId);
        refreshTokenRepository.save(new RefreshToken(userId, initialToken.refreshToken()));

        // when
        TokenResponse result = sut.reissueToken(initialToken.refreshToken());

        // then
        RefreshToken stored = refreshTokenRepository.findByUserId(userId).orElseThrow();
        assertThat(stored.isSameToken(result.refreshToken())).isTrue();
        assertThat(stored.isSameToken(initialToken.refreshToken())).isFalse();
    }

    @Test
    void DB에_리프레쉬_토큰이_없으면_UnauthorizedException을_발생시킨다() {
        // given
        Long userId = 1L;
        TokenResponse initialToken = tokenProvider.generateToken(userId);

        // when & then
        assertThatThrownBy(() -> sut.reissueToken(initialToken.refreshToken()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("인증되지 않은 사용자입니다.");
    }

    @Test
    void 저장된_토큰과_다른_토큰으로_재발급_요청시_예외가_발생한다() {
        // given
        Long userId = 1L;
        TokenResponse originalToken = tokenProvider.generateToken(userId);
        refreshTokenRepository.save(new RefreshToken(userId, originalToken.refreshToken()));

        // when
        TokenResponse invalidAttemptToken = tokenProvider.generateToken(userId);

        // when & then
        assertThatThrownBy(() -> sut.reissueToken(invalidAttemptToken.refreshToken()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("인증되지 않은 사용자입니다.");
    }

    @Test
    void 만료된_리프레쉬_토큰이면_예외를_발생시킨다() throws InterruptedException {
        // given
        TokenProvider expiredTokenProvider = new JwtProvider(secretKey, 3600L, 0L);
        Long userId = 1L;

        TokenResponse expiredToken = expiredTokenProvider.generateToken(userId);
        refreshTokenRepository.save(new RefreshToken(userId, expiredToken.refreshToken()));

        // when & then
        assertThatThrownBy(() -> sut.reissueToken(expiredToken.refreshToken()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("만료된 토큰입니다.");
    }

    @Test
    void 재발급_후_이전_토큰_재사용은_실패한다() {
        // given
        Long userId = 1L;
        TokenResponse initial = tokenProvider.generateToken(userId);
        refreshTokenRepository.save(new RefreshToken(userId, initial.refreshToken()));

        // when
        sut.reissueToken(initial.refreshToken());

        // then
        assertThatThrownBy(() -> sut.reissueToken(initial.refreshToken()))
            .isInstanceOf(UnauthorizedException.class);
    }
}
