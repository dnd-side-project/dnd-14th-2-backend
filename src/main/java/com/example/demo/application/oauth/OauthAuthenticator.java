package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OauthAuthenticator {

    private final IdTokenVerifier idTokenVerifier;

    @Transactional
    public OauthUserInfo authenticate(Provider provider, String idToken) {
        return idTokenVerifier.verifyAndGetUserInfo(provider, idToken);
    }
}
