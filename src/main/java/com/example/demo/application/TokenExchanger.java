package com.example.demo.application;

import com.example.demo.application.dto.OauthToken;

public interface TokenExchanger {

    OauthToken exchange(String authorizationCode);
}
