package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.dto.OauthToken;
import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.oauth.TokenExchanger;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KakaoOauthServiceTest {

    @InjectMocks
    private KakaoOauthService kakaoOauthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenExchanger kakaoTokenExchanger;

    @Mock
    private IdTokenVerifier kakaoIdTokenVerifier;

    @Test
    void 가입된_사용자가_있는_경우_조회_후_반환() {
        // given
        String authCode = "auth-code";
        String idToken = "kakao-id-token";
        String accessToken = "kakao-id-token";
        String refreshToken = "kakao-id-token";
        OauthToken oauthToken = new OauthToken(idToken, accessToken, refreshToken);
        OauthUserInfo userInfo = new OauthUserInfo("kakao-provider-id", "test@kakao.com", "http://image.png");
        User existingUser = new User("test@kakao.com", "http://image.png", Provider.KAKAO, "kakao-provider-id");

        given(kakaoTokenExchanger.exchange(authCode)).willReturn(oauthToken);
        given(kakaoIdTokenVerifier.verifyAndGetUserInfo(Provider.KAKAO, idToken)).willReturn(userInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, userInfo.providerId()))
                .willReturn(Optional.of(existingUser));

        // when
        User result = kakaoOauthService.getUserInfo(authCode);

        // then
        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, never()).save(any()); // 기존 사용자가 있으니 저장은 호출되지 않아야 함
    }

    @Test
    void 신규_사용자인_경우_저장_후_반환() {
        // given
        String authCode = "auth-code";
        String idToken = "kakao-id-token";
        String accessToken = "kakao-id-token";
        String refreshToken = "kakao-id-token";
        OauthToken oauthToken = new OauthToken(idToken, accessToken, refreshToken);
        OauthUserInfo userInfo = new OauthUserInfo("kakao-id-new", "new@kakao.com", "http://new-image.png");
        User newUser = new User("new@kakao.com", "http://new-image.png", Provider.KAKAO, "kakao-id-new");

        given(kakaoTokenExchanger.exchange(authCode)).willReturn(oauthToken);
        given(kakaoIdTokenVerifier.verifyAndGetUserInfo(Provider.KAKAO, idToken)).willReturn(userInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, userInfo.providerId()))
                .willReturn(Optional.empty()); // DB에 없음
        given(userRepository.save(any(User.class))).willReturn(newUser);

        // when
        User result = kakaoOauthService.getUserInfo(authCode);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(newUser);
        verify(userRepository, times(1)).save(any(User.class));
    }
}