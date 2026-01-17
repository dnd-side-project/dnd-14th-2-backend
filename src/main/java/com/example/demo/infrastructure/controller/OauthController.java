package com.example.demo.infrastructure.controller;

import com.example.demo.application.LoginService;
import com.example.demo.infrastructure.controller.dto.AuthTokenWebResponse;
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

    private final LoginService loginService;

    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam(name = "code") String authorizationCode) {
        AuthTokenWebResponse response = loginService.kakaoLogin(authorizationCode);
        return ResponseEntity.ok(response);
    }

}
