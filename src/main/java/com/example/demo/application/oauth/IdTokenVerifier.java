package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthUserInfo;

public interface IdTokenVerifier {

    OauthUserInfo verify(String idToken);
}
