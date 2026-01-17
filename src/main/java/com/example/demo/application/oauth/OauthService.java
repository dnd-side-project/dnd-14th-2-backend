package com.example.demo.application.oauth;

import com.example.demo.domain.User;

public interface OauthService {

    User getUserInfo(String authorizationCode);
}
