package com.example.demo.application.oauth;

import com.example.demo.application.dto.TokenResponse;

public interface TokenProvider {

    TokenResponse generateToken(Long userId);

    Long validateAccessToken(String accessToken);

    Long validateRefreshToken(String refreshToken);
}
