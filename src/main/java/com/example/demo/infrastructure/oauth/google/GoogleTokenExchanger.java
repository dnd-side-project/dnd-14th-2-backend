package com.example.demo.infrastructure.oauth.google;

import com.example.demo.application.oauth.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleTokenExchanger implements TokenExchanger {

    private final RestClient googleOauthRestClient;
    private final GoogleOauthProperties googleOauthProperties;

    @Override
    public OauthToken exchange(String authorizationCode) {
        return googleOauthRestClient.post()
            .uri("/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(tokenBody(authorizationCode))
            .retrieve()
            .toEntity(OauthToken.class)
            .getBody();
    }

    private MultiValueMap<String, String> tokenBody(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", googleOauthProperties.clientId());
        params.add("client_secret", googleOauthProperties.clientSecret());
        params.add("redirect_uri", googleOauthProperties.redirectUri());
        params.add("grant_type", "authorization_code");

        return params;
    }
}
