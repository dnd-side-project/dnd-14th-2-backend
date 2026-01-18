package com.example.demo.application.oauth;

import com.example.demo.domain.Provider;
import com.example.demo.domain.User;

public interface OauthService {

    Provider provider();

    User getUserInfo(String authorizationCode);
}
