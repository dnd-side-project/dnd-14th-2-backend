package com.example.demo.infrastructure.oauth.google;

import com.example.demo.application.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleTokenExchanger implements TokenExchanger {

    private static final String GRANT_TYPE = "authorization_code";

    private final RestClient googleOauthRestClient;
    private final GoogleOauthProperties googleOauthProperties;

    public GoogleTokenExchanger(RestClient googleOauthRestClient,
                                GoogleOauthProperties googleOauthProperties) {
        this.googleOauthRestClient = googleOauthRestClient;
        this.googleOauthProperties = googleOauthProperties;
    }

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
        params.add("grant_type", GRANT_TYPE);

        return params;
    }
}
