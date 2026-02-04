package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.domain.Provider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.util.AbstractIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private IdTokenVerifier idTokenVerifier;

    @Test
    void 기존_사용자는_로그인_할_수_있다() {
        // given
        String idToken = "test-id-token";
        given(idTokenVerifier.verifyAndGetUserInfo(Provider.GOOGLE, idToken)).willReturn(
            new OauthUserInfo("test-provider-id", "test@email.com", "http://test.jpg")
        );

        User user = new User("test@email.com", "http://test.jpg", Provider.GOOGLE, "test-provider-id");
        User savedUser = userRepository.save(user);

        // when
        TokenResponse tokenResponse = sut.login(Provider.GOOGLE, idToken);

        // then
        assertThat(refreshTokenRepository.findByUserId(user.getId()))
            .hasValueSatisfying(refreshToken -> {
                assertThat(refreshToken).isNotNull();
                assertThat(refreshToken)
                    .extracting("token")
                    .isEqualTo(tokenResponse.refreshToken());
            });
    }

    @Test
    void 새로운_사용자가_로그인하면_자동_회원가입_된다() {
        // given
        String idToken = "test-id-token";
        given(idTokenVerifier.verifyAndGetUserInfo(Provider.GOOGLE, idToken)).willReturn(
            new OauthUserInfo("test-provider-id", "test@email.com", "http://test.jpg")
        );

        // when
        TokenResponse tokenResponse = sut.login(Provider.GOOGLE, idToken);

        // then
        assertThat(userRepository.findByProviderAndProviderId(Provider.GOOGLE, "test-provider-id"))
            .hasValueSatisfying(user -> {

                assertThat(refreshTokenRepository.findByUserId(user.getId()))
                    .hasValueSatisfying(refreshToken -> {
                        assertThat(refreshToken).isNotNull();
                        assertThat(refreshToken)
                            .extracting("token")
                            .isEqualTo(tokenResponse.refreshToken());
                    });
            });
    }
}
