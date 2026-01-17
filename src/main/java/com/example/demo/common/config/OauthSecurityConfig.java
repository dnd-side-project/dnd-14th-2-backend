package com.example.demo.common.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;

@Configuration
public class OauthSecurityConfig {

    @Bean
    JWKSource<SecurityContext> kakaoJwkSource() {
        return JWKSourceBuilder
                .create(toUrl("https://kauth.kakao.com/.well-known/jwks.json"))
                .build();
    }

    private static URL toUrl(String url) {
        try {
            return URI.create(url).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new IllegalStateException("JWKS URL 설정이 올바르지 않습니다: " + url, e);
        }
    }
}
