package com.example.demo.infrastructure;

import com.example.demo.application.OauthService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleOauthService implements OauthService {

    private static final String GRANT_TYPE = "authorization_code";

    private final RestClient googleOauthRestClient;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final UserRepository userRepository;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOauthService(RestClient googleOauthRestClient,
                              GoogleIdTokenVerifierService googleIdTokenVerifierService,
                              UserRepository userRepository,
                              @Value("${google.client-id}") String clientId,
                              @Value("${google.client-secret}") String clientSecret,
                              @Value("${google.redirection-url}") String redirectUri) {
        this.googleOauthRestClient = googleOauthRestClient;
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.userRepository = userRepository;
    }

    // User Return -> OSIV
    @Override
    public User getUserInfo(String authorizationCode) {
        GoogleTokenInfo googleTokenInfo = requestGoogleTokenInfo(authorizationCode);
        Payload result = googleIdTokenVerifierService.verify(googleTokenInfo.id_token());

        String providerId = result.getSubject();
        String email = result.getEmail();
        String picture = result.get("picture").toString();

        User user = userRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId)
            .orElseGet(() -> userRepository.save(
                new User(
                    email,
                    picture,
                    Provider.GOOGLE,
                    providerId
                )
            ));

        return user;
    }

    private GoogleTokenInfo requestGoogleTokenInfo(String authorizationCode) {
        return googleOauthRestClient.post()
            .uri("/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(tokenBody(authorizationCode))
            .retrieve()
            .toEntity(GoogleTokenInfo.class)
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
