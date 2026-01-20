package com.example.demo.application.oauth;

import com.example.demo.application.dto.OauthToken;
import com.example.demo.domain.Provider;

public interface TokenExchanger {

    Provider provider();

    OauthToken exchange(String authorizationCode);
}
