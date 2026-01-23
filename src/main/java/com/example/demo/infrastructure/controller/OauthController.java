package com.example.demo.infrastructure.controller;

import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
import com.example.demo.infrastructure.controller.dto.OauthLoginWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

    private final OauthService oauthService;
    private final AuthService authService;

    @PostMapping("/oauth/login")
    public ResponseEntity<AuthTokenWebResponse> oauthLogin(@Valid @RequestBody OauthLoginWebRequest request) {
        User userInfo = oauthService.getUserInfo(request.provider(), request.idToken());
        TokenResponse token = authService.issueTokens(userInfo);

        return ResponseEntity.ok(AuthTokenWebResponse.from(token));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }
}
