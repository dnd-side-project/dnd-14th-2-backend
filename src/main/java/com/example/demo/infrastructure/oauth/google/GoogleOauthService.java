package com.example.demo.infrastructure.oauth.google;

import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.application.oauth.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOauthService implements OauthService {

    private final TokenExchanger tokenExchanger;
    private final IdTokenVerifier idTokenVerifier;
    private final UserRepository userRepository;

    @Override
    public User getUserInfo(String authorizationCode) {
        OauthToken oauthToken = tokenExchanger.exchange(authorizationCode);
        if (oauthToken == null || oauthToken.idToken() == null) {
            throw new IllegalArgumentException("인가 코드를 토큰으로 교환하는데 실패했습니다. (id_token을 찾을 수 없음)");
        }

        OauthUserInfo oauthUserInfo = idTokenVerifier.verifyAndGetUserInfo(Provider.GOOGLE, oauthToken.idToken());

        User user = userRepository.findByProviderAndProviderId(Provider.GOOGLE, oauthUserInfo.providerId())
            .orElseGet(() -> userRepository.save(
                new User(
                    oauthUserInfo.email(),
                    oauthUserInfo.picture(),
                    Provider.GOOGLE,
                    oauthUserInfo.providerId()
                )
            ));

        return user;
    }
}
