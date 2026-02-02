package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OauthAuthenticator {

    private final IdTokenVerifier idTokenVerifier;
    private final UserRepository userRepository;

    @Transactional
    public User getUserInfo(Provider provider, String idToken) {
        OauthUserInfo userInfo = idTokenVerifier.verifyAndGetUserInfo(provider, idToken);

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
