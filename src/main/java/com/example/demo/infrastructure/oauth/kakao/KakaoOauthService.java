package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.oauth.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class KakaoOauthService implements OauthService {
    private final UserRepository userRepository;
    private final TokenExchanger kakaoTokenExchanger;
    private final IdTokenVerifier OidcIdTokenVerifierService;

    @Override
    public User getUserInfo(String authorizationCode) {
        OauthToken oauthToken = kakaoTokenExchanger.exchange(authorizationCode);
        OauthUserInfo userInfo = OidcIdTokenVerifierService.verifyAndGetUserInfo(Provider.KAKAO, oauthToken.idToken());

        return userRepository.findByProviderAndProviderId(Provider.KAKAO, userInfo.providerId())
                .orElseGet(() -> userRepository.save(
                        new User(
                                userInfo.email(),
                                userInfo.picture(),
                                Provider.KAKAO,
                                userInfo.providerId()
                        )
                ));
    }

}
