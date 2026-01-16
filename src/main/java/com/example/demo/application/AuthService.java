package com.example.demo.application;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse issueTokens(User user) {
        TokenResponse tokenResponse = tokenProvider.generateToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(
            user.getId(),
            tokenResponse.refreshToken()
        ));

        return tokenResponse;
    }
}
