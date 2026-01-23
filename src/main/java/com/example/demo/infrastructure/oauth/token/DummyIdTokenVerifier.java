package com.example.demo.infrastructure.oauth.token;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.domain.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class DummyIdTokenVerifier implements IdTokenVerifier {

    @Override
    public OauthUserInfo verifyAndGetUserInfo(Provider provider, String idToken) {
        return new OauthUserInfo("dummy-provider-id", "dummy-email", "dummy-picture");
    }
}
