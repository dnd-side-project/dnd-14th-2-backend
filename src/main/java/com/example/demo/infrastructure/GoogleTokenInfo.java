package com.example.demo.infrastructure;

public record GoogleTokenInfo(
    String access_token,
    Long expires_in,
    String token_type,
    String scope,
    String refresh_token
) {
}
