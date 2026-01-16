package com.example.demo.infrastructure.adapter;

import com.example.demo.application.OauthService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.infrastructure.adapter.dto.KakaoTokenResponse;
import com.example.demo.infrastructure.adapter.dto.KakaoUserInfoResponse;
import com.example.demo.infrastructure.properties.KakaoOauthProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class KakaoOauthService implements OauthService {
    private final UserRepository userRepository;
    private final RestClient kakaoTokenRestClient;
    private final KakaoOauthProperties kakaoOauthProperties;

    @Override
    public User getUserInfo(String authorizationCode) {
        KakaoTokenResponse kakaoTokenResponse = requestToken(authorizationCode);
        KakaoUserInfoResponse userInfo = parseToken(kakaoTokenResponse.idToken());

        return userRepository.findByProviderAndProviderId(Provider.KAKAO, userInfo.id())
                .orElseGet(() -> userRepository.save(new User(userInfo.email(), userInfo.picture(), Provider.KAKAO, userInfo.id())));
    }

    private KakaoTokenResponse requestToken(String authorizationCode) {
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
                .body(KakaoTokenResponse.class);
    }


    private KakaoUserInfoResponse parseToken(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String sub = claims.getSubject();
            String email = claims.getStringClaim("email");
            String picture = claims.getStringClaim("picture");
            return new KakaoUserInfoResponse(sub, email, picture);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid id_token", e);
        }
    }
}
