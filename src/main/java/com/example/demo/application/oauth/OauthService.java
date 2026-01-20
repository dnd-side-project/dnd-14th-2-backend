package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthToken;
import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OauthService {

    private final TokenExchangerFactory exchangerFactory;
    private final IdTokenVerifier OidcIdTokenVerifierService;
    private final UserRepository userRepository;

    @Transactional
    public User getUserInfo(Provider provider, String authorizationCode) {
        OauthToken oauthToken = exchangerFactory.get(provider).exchange(authorizationCode);
        if (oauthToken == null || oauthToken.idToken() == null) {
            throw new IllegalArgumentException("인가 코드를 토큰으로 교환하는데 실패했습니다. (id_token을 찾을 수 없음)");
        }

        OauthUserInfo userInfo = OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, oauthToken.idToken());

        return userRepository.findByProviderAndProviderId(provider, userInfo.providerId())
                .orElseGet(() -> userRepository.save(
                        new User(
                                userInfo.email(),
                                userInfo.picture(),
                                provider,
                                userInfo.providerId()
                        )
                ));
    }
}
