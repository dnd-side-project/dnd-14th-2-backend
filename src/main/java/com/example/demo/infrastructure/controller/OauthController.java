package com.example.demo.infrastructure.controller;

import com.example.demo.application.AuthService;
import com.example.demo.application.OauthService;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

    private final OauthService oauthService;
    private final AuthService authService;

    @PostMapping("/oauth/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestParam("code") String authorizationCode) {
        User userInfo = oauthService.getUserInfo(authorizationCode);
        TokenResponse tokenResponse = authService.issueTokens(userInfo);

        return ResponseEntity.ok(tokenResponse);
    }
}
