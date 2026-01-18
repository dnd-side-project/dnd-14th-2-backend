package com.example.demo.infrastructure.controller;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthServiceFactory;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.controller.dto.OauthLoginWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OauthController {

    private final OauthServiceFactory oauthServiceFactory;
    private final AuthService authService;


    @PostMapping("/oauth/login")
    public ResponseEntity<TokenResponse> oauthLogin(@Valid @RequestBody OauthLoginWebRequest request) {
        Provider provider = request.provider();
        User userInfo = oauthServiceFactory.get(provider).getUserInfo(request.code());
        TokenResponse tokenResponse = authService.issueTokens(userInfo);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }

}
