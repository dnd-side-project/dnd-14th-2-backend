package com.example.demo.infrastructure.controller.dto;

public record AuthTokenWebResponse(
    String accessToken,
    String refreshToken
) {
}
