package com.example.demo.application;

import com.example.demo.application.dto.OauthUserInfo;

public interface IdTokenVerifier {
    OauthUserInfo verify(String idToken);
}
