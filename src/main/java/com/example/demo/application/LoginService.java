package com.example.demo.application;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final OauthService kakaoOauthService;
    private final AuthService authService;

    public AuthTokenWebResponse kakaoLogin(String code) {
        User user = kakaoOauthService.getUserInfo(code);
        TokenResponse token = authService.issueTokens(user);
        return new AuthTokenWebResponse(token.accessToken(), token.refreshToken());
    }
}
