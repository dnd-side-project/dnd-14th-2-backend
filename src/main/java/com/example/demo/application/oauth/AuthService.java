package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.user.UserService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final OauthAuthenticator oauthAuthenticator;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse login(Provider provider, String idToken) {
        OauthUserInfo userInfo = oauthAuthenticator.authenticate(provider, idToken);
        User user = userService.findOrCreateUser(provider, userInfo);

        return issueTokens(user.getId());
    }

    private TokenResponse issueTokens(Long userId) {
        TokenResponse token = tokenProvider.generateToken(userId);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
            .orElseGet(() -> new RefreshToken(userId, token.refreshToken()));

        refreshToken.rotate(token.refreshToken());
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
