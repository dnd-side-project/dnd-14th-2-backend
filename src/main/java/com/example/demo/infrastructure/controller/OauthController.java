package com.example.demo.infrastructure.controller;

import com.example.demo.application.OauthService;
import com.example.demo.application.TokenProvider;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OauthController {

    private final OauthService kakaoOauthService;
    private final TokenProvider tokenProvider;

    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam @NotBlank String code) {
        log.info("[kakaoLogin] authorization code = {}", code);
        User user = kakaoOauthService.getUserInfo(code);
        TokenResponse token = tokenProvider.generateToken(user.getId());
        AuthTokenWebResponse response = new AuthTokenWebResponse(token.accessToken(), token.refreshToken());
        return ResponseEntity.ok(response);
    }
}
