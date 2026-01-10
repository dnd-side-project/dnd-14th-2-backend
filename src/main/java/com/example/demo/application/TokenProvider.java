package com.example.demo.application;

import com.example.demo.application.dto.TokenResponse;

public interface TokenProvider {

    TokenResponse generateToken(Long userId);

    Long validateToken(String accessToken);
}
