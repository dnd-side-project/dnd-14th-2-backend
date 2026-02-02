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
    void 토큰을_발급_받을_수_있다() {
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
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(savedUser.getId());
        assertThat(refreshToken.get())
            .isNotNull();
        assertThat(refreshToken.get())
            .extracting("token")
            .isEqualTo(tokenResponse.refreshToken());
    }
}
