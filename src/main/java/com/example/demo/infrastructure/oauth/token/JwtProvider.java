package com.example.demo.infrastructure.oauth.token;

import com.example.demo.application.TokenProvider;
import com.example.demo.application.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {

    private final String jwtSecretKey;

    public JwtProvider(@Value("${jwt.secret-key}") String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public TokenResponse generateToken(Long userId) {
        long now = System.currentTimeMillis();
        String accessToken = Jwts.builder()
            .claim("userId", userId)
            .issuedAt(new Date(now))
            .expiration(new Date(now + 43200000))
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact();

        String refreshToken = Jwts.builder()
            .claim("userId", userId)
            .issuedAt(new Date(now))
            .expiration(new Date(now + 5260000000L))
            .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
            .compact();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public Long validateToken(String accessToken) {
        try {
            Claims payload = Jwts.parser()
                .verifyWith(
                    Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey)))
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

            return payload.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException();
        } catch (JwtException e) {
            throw new RuntimeException();
        }
    }
}
