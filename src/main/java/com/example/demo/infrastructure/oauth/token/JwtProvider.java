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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {

    private final String jwtSecretKey;
    private final Long accessTokenExpireRange;
    private final Long refreshTokenExpireRange;

    public JwtProvider(@Value("${jwt.secret.key}") String jwtSecretKey,
                       @Value("${jwt.secret.access.expire-range}") Long accessTokenExpireRange,
                       @Value("${jwt.secret.refresh.expire-range}") Long refreshTokenExpireRange) {
        this.jwtSecretKey = jwtSecretKey;
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
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact();

        String refreshToken = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .claim("userId", userId)
            .claim("typ", "refresh")
            .issuedAt(new Date(now))
            .expiration(new Date(now + refreshTokenExpireRange))
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public Long validateToken(String token) {
        try {
            Claims payload = Jwts.parser()
                .verifyWith(
                    Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return payload.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new UnauthorizedException("유효하지 않은 토큰 정보입니다.");
        }
    }
}
