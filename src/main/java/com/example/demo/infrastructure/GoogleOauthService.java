package com.example.demo.infrastructure;

import com.example.demo.application.OauthService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleOauthService implements OauthService {

    private static final String GRANT_TYPE = "authorization_code";

    private final RestClient googleOauthRestClient;
    private final RestClient googleApiRestClient;
    private final UserRepository userRepository;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOauthService(RestClient googleOauthRestClient,
                              RestClient googleApiRestClient,
                              UserRepository userRepository,
                              @Value("${google.client-id}") String clientId,
                              @Value("${google.client-secret}") String clientSecret,
                              @Value("${google.redirection-url}") String redirectUri) {
        this.googleOauthRestClient = googleOauthRestClient;
        this.googleApiRestClient = googleApiRestClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.userRepository = userRepository;
    }

    // User Return -> OSIV
    @Override
    public User getUserInfo(String authorizationCode) {
        String googleAccessToken = requestGoogleAccessToken(authorizationCode);

        GoogleUserInfo userInfo = requestGoogleUserInfo(googleAccessToken);

        User user = userRepository.findByProviderAndProviderId(Provider.GOOGLE, userInfo.id())
            .orElseGet(() -> userRepository.save(
                new User(
                    userInfo.email(),
                    userInfo.picture(),
                    Provider.GOOGLE,
                    userInfo.id()
                )
            ));

        return user;
    }

    @Nullable
    private GoogleUserInfo requestGoogleUserInfo(String googleAccessToken) {
        return googleApiRestClient.get()
            .uri("/oauth2/v2/userinfo")
            .header("Authorization", "Bearer " + googleAccessToken)
            .retrieve()
            .toEntity(GoogleUserInfo.class)
            .getBody();
    }

    private String requestGoogleAccessToken(String authorizationCode) {
        GoogleTokenInfo body = googleOauthRestClient.post()
            .uri("/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(tokenBody(authorizationCode))
            .retrieve()
            .toEntity(GoogleTokenInfo.class)
            .getBody();

        return body.access_token();
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
