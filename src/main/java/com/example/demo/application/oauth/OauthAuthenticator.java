package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.user.UserService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OauthAuthenticator {

    private final IdTokenVerifier idTokenVerifier;
    private final UserService userService;

    @Transactional
    public User getUserInfo(Provider provider, String idToken) {
        OauthUserInfo userInfo = idTokenVerifier.verifyAndGetUserInfo(provider, idToken);
        return userService.login(provider, userInfo);
    }
}
