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
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleTokenExchanger(RestClient googleOauthRestClient,
                                @Value("${google.client-id}") String clientId,
                                @Value("${google.client-secret}") String clientSecret,
                                @Value("${google.redirection-url}") String redirectUri) {
        this.googleOauthRestClient = googleOauthRestClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
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
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", GRANT_TYPE);

        return params;
    }
}
