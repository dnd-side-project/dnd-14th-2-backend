package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;

public interface IdTokenVerifier {

    OauthUserInfo verifyAndGetUserInfo(Provider provider, String idToken);
}
