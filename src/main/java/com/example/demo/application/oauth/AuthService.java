package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.user.UserService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.application.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final OauthAuthenticator oauthAuthenticator;
    private final UserService userService;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse login(Provider provider, String idToken) {
        OauthUserInfo userInfo = oauthAuthenticator.authenticate(provider, idToken);
        return processLogin(provider, userInfo);
    }

    private TokenResponse processLogin(Provider provider, OauthUserInfo userInfo) {
        try {
            User user = userService.findOrCreateUser(provider, userInfo);
            return tokenIssuer.issueTokens(user.getId());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("닉네임/초대코드 중복으로 인해 새로운 유저 생성에 실패했습니다.", e);
        }
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenResponse reissueToken(String refreshToken) {
        Long userId = tokenProvider.validateRefreshToken(refreshToken);
        RefreshToken findRefreshToken = refreshTokenRepository.findByUserId(userId)
            .filter(token -> token.isSameToken(refreshToken))
            .orElseThrow(() -> new UnauthorizedException("인증되지 않은 사용자입니다."));

        log.info("토큰 재발급 요청 userId: {}", userId);

        TokenResponse tokenResponse = tokenProvider.generateToken(userId);
        findRefreshToken.rotate(tokenResponse.refreshToken());
        refreshTokenRepository.save(findRefreshToken);

        return tokenResponse;
    }
}
