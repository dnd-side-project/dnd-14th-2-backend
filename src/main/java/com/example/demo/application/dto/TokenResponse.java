package com.example.demo.application.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {
}
