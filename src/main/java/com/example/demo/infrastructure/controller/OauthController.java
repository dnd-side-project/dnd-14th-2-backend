package com.example.demo.infrastructure.controller;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
import com.example.demo.infrastructure.interceptor.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OauthController {

    private final OauthService kakaoOauthService;
    private final OauthService googleOauthService;
    private final AuthService authService;

    @PostMapping("/oauth/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestParam("code") String authorizationCode) {
        User userInfo = googleOauthService.getUserInfo(authorizationCode);
        TokenResponse tokenResponse = authService.issueTokens(userInfo);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam(name = "code") String authorizationCode) {
        User user = kakaoOauthService.getUserInfo(authorizationCode);
        TokenResponse tokenResponse = authService.issueTokens(user);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }

}
