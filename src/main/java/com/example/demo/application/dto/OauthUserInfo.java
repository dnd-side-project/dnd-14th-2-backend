package com.example.demo.application.dto;

public record OauthUserInfo(
    String providerId,
    String email,
    String picture
) {
}
