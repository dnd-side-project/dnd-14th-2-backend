package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthToken;

public interface TokenExchanger {

    OauthToken exchange(String authorizationCode);
}
