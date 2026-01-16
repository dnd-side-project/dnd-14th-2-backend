package com.example.demo.infrastructure.oauth.google;

import com.example.demo.application.OauthService;
import com.example.demo.application.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.springframework.stereotype.Service;

@Service
public class GoogleOauthService implements OauthService {

    private final TokenExchanger tokenExchanger;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final UserRepository userRepository;

    public GoogleOauthService(TokenExchanger tokenExchanger,
                              GoogleIdTokenVerifierService googleIdTokenVerifierService,
                              UserRepository userRepository
    ) {
        this.tokenExchanger = tokenExchanger;
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
        this.userRepository = userRepository;
    }

    @Override
    public User getUserInfo(String authorizationCode) {
        OauthToken oauthToken = tokenExchanger.exchange(authorizationCode);
        if (oauthToken == null || oauthToken.idToken() == null) {
            throw new IllegalArgumentException("인가 코드를 토큰으로 교환하는데 실패했습니다. (id_token을 찾을 수 없음)");
        }

        Payload result = googleIdTokenVerifierService.verify(oauthToken.idToken());

        String providerId = result.getSubject();
        String email = result.getEmail();
        String picture = (String) result.get("picture");

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
}
