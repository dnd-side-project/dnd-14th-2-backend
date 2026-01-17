package com.example.demo.application.dto;

public record OauthToken(
        String idToken,
        String accessToken,
        String refreshToken
) {
}