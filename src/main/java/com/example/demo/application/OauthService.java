package com.example.demo.application;

import com.example.demo.domain.User;

public interface OauthService {

    User getUserInfo(String authorizationCode);
}
