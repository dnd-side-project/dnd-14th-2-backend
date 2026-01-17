package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.TokenExchanger;
import com.example.demo.application.dto.OauthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class KakaoTokenExchanger implements TokenExchanger {

    private final RestClient kakaoTokenRestClient;
    private final KakaoOauthProperties kakaoOauthProperties;

    @Override
    public OauthToken exchange(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoOauthProperties.clientId());
        form.add("redirect_uri", kakaoOauthProperties.redirectUri());
        form.add("code", authorizationCode);
        form.add("client_secret", kakaoOauthProperties.clientSecret());

        return kakaoTokenRestClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(OauthToken.class);
    }
}
