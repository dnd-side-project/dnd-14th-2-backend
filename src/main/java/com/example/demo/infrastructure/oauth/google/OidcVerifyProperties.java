package com.example.demo.infrastructure.oauth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.oidc")
public record OidcVerifyProperties(
    OidcProperties google
) {

    public record OidcProperties(
        String issuer,
        String audience,
        String jwksUri
    ) {}
}
