package com.example.demo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OauthToken(
    @JsonProperty("id_token")
    String idToken,
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken
) {
}
