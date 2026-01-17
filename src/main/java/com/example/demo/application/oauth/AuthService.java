package com.example.demo.application.oauth;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse issueTokens(User user) {
        TokenResponse token = tokenProvider.generateToken(user.getId());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId((user.getId()))
                .orElseGet(() -> new RefreshToken(user.getId(), token.refreshToken()));

        refreshToken.rotate(token.refreshToken());
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
