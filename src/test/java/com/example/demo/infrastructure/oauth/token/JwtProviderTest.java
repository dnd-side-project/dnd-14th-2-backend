package com.example.demo.infrastructure.oauth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private final String secretKey = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3NlLW9ubHktbG9uZy1lbm91Z2g=";
    private final Long accessTokenExpireRange = 3600000L;
    private final Long refreshTokenExpireRange = 604800000L;

    private JwtProvider sut = new JwtProvider(secretKey, accessTokenExpireRange, refreshTokenExpireRange);

    @Test
    void userId로_토큰을_생성하면_access_token과_refresh_token이_반환된다() {
        // given
        Long userId = 1L;

        // when
        TokenResponse tokenResponse = sut.generateToken(userId);

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotNull().isNotEmpty();
        assertThat(tokenResponse.refreshToken()).isNotNull().isNotEmpty();
    }

    @Test
    void 생성된_access_token에는_userId가_포함되어_있다() {
        // given
        Long userId = 1L;

        // when
        TokenResponse tokenResponse = sut.generateToken(userId);
        Long extractedUserId = sut.validateAccessToken(tokenResponse.accessToken());

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void 생성된_refresh_token에는_userId가_포함되어_있다() {
        // given
        Long userId = 1L;

        // when
        TokenResponse tokenResponse = sut.generateToken(userId);
        Long extractedUserId = sut.validateRefreshToken(tokenResponse.refreshToken());

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void refresh_token을_access_token으로_검증하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        TokenResponse tokenResponse = sut.generateToken(userId);

        // when & then
        assertThatThrownBy(() -> sut.validateAccessToken(tokenResponse.refreshToken()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("잘못된 토큰 타입입니다.");
    }

    @Test
    void 만료된_access_token을_검증하면_예외가_발생한다() {
        // given
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        long pastTime = System.currentTimeMillis() - 10000000L;
        String expiredToken = Jwts.builder()
            .claim("userId", 1L)
            .claim("typ", "access")
            .issuedAt(new Date(pastTime))
            .expiration(new Date(pastTime + 1000L))
            .signWith(key)
            .compact();

        // when & then
        assertThatThrownBy(() -> sut.validateAccessToken(expiredToken))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("만료된 토큰입니다.");
    }

    @Test
    void 잘못된_형식의_토큰을_검증하면_예외가_발생한다() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThatThrownBy(() -> sut.validateAccessToken(invalidToken))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("유효하지 않은 토큰 정보입니다.");
    }

    @Test
    void userId가_없는_토큰을_검증하면_예외가_발생한다() {
        // given
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        long now = System.currentTimeMillis();
        String tokenWithoutUserId = Jwts.builder()
            .claim("typ", "access")
            .issuedAt(new Date(now))
            .expiration(new Date(now + accessTokenExpireRange))
            .signWith(key)
            .compact();

        // when & then
        assertThatThrownBy(() -> sut.validateAccessToken(tokenWithoutUserId))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("유효하지 않은 토큰 정보입니다.");
    }

    @Test
    void 다른_secret_key로_서명된_토큰을_검증하면_예외가_발생한다() {
        // given
        String differentSecretKey = "ZGlmZmVyZW50LXNlY3JldC1rZXktZm9yLXRlc3RpbmctcHVycG9zZS1vbmx5LWxvbmctZW5vdWdo";
        SecretKey differentKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecretKey));
        long now = System.currentTimeMillis();
        String tokenWithDifferentKey = Jwts.builder()
            .claim("userId", 1L)
            .claim("typ", "access")
            .issuedAt(new Date(now))
            .expiration(new Date(now + accessTokenExpireRange))
            .signWith(differentKey)
            .compact();

        // when & then
        assertThatThrownBy(() -> sut.validateAccessToken(tokenWithDifferentKey))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("유효하지 않은 토큰 정보입니다.");
    }

    @Test
    void access_token을_refresh_token으로_검증하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        TokenResponse tokenResponse = sut.generateToken(userId);

        // when & then
        assertThatThrownBy(() -> sut.validateRefreshToken(tokenResponse.accessToken()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("잘못된 토큰 타입입니다.");
    }

    @Test
    void 만료된_refresh_token을_검증하면_예외가_발생한다() {
        // given
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        long pastTime = System.currentTimeMillis() - 10000000L;
        String expiredToken = Jwts.builder()
            .claim("userId", 1L)
            .claim("typ", "refresh")
            .issuedAt(new Date(pastTime))
            .expiration(new Date(pastTime + 1000L))
            .signWith(key)
            .compact();

        // when & then
        assertThatThrownBy(() -> sut.validateRefreshToken(expiredToken))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("만료된 토큰입니다.");
    }
}
