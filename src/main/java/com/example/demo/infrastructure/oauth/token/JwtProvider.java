package com.example.demo.infrastructure.oauth.token;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.infrastructure.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {

    private final SecretKey jwtSecretKey;
    private final Long accessTokenExpireRange;
    private final Long refreshTokenExpireRange;

    public JwtProvider(@Value("${jwt.secret.key}") String jwtSecretKey,
                       @Value("${jwt.secret.access.expire-range}") Long accessTokenExpireRange,
                       @Value("${jwt.secret.refresh.expire-range}") Long refreshTokenExpireRange) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey));
        this.accessTokenExpireRange = accessTokenExpireRange;
        this.refreshTokenExpireRange = refreshTokenExpireRange;
    }

    public TokenResponse generateToken(Long userId) {
        long now = System.currentTimeMillis();
        String accessToken = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .claim("userId", userId)
            .claim("typ", "access")
            .issuedAt(new Date(now))
            .expiration(new Date(now + accessTokenExpireRange))
            .signWith(jwtSecretKey)
            .compact();

        String refreshToken = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .claim("userId", userId)
            .claim("typ", "refresh")
            .issuedAt(new Date(now))
            .expiration(new Date(now + refreshTokenExpireRange))
            .signWith(jwtSecretKey)
            .compact();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public Long validateAccessToken(String accessToken) {
        return validateToken(accessToken, "access");
    }

    @Override
    public Long validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, "refresh");
    }

    private Long validateToken(String token, String type) {
        try {
            Claims payload = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            validateTokenType(type, payload.get("typ", String.class));
            return payload.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new UnauthorizedException("유효하지 않은 토큰 정보입니다.");
        }
    }

    private void validateTokenType(String type, String payloadType) {
        if (type.equals(payloadType)) {
            throw new UnauthorizedException("잘못된 토큰 타입입니다.");
        }
    }
}
