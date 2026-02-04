package com.example.demo.infrastructure.controller;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
import com.example.demo.infrastructure.controller.dto.OauthLoginWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/oauth/login")
    public ResponseEntity<AuthTokenWebResponse> oauthLogin(@Valid @RequestBody OauthLoginWebRequest request) {
        TokenResponse token = authService.login(request.provider(), request.idToken());

        return ResponseEntity.ok(AuthTokenWebResponse.from(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }
}
