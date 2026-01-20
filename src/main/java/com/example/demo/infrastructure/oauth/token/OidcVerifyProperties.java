package com.example.demo.infrastructure.oauth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.oidc")
public record OidcVerifyProperties(
    OidcProperties google,
    OidcProperties kakao
) {
    public record OidcProperties(
        String issuer,
        String audience,
        String jwksUri
    ) {}
}
