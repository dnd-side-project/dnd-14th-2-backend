package com.example.demo.infrastructure.oauth.google;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(value = "oauth.google")
public record GoogleOauthProperties(
    @NotBlank String clientId,
    @NotBlank String clientSecret,
    @NotBlank String redirectUri
) {
}
