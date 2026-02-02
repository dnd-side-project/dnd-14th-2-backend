package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.oauth.OauthAuthenticator;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class OauthAuthenticatorTest extends AbstractIntegrationTest {

    @MockitoBean
    private IdTokenVerifier OidcIdTokenVerifierService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthAuthenticator sut;


    @Test
    void idToken으로_새로운_사용자_정보를_가져와_저장한다() {
        // given
        Provider provider = Provider.GOOGLE;
        String idToken = "test-id-token";
        String providerId = "google-123456";
        String email = "test@example.com";
        String picture = "https://example.com/picture.jpg";

        OauthUserInfo oauthUserInfo = new OauthUserInfo(providerId, email, picture);
        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken)).willReturn(oauthUserInfo);

        // when
        User result = sut.getUserInfo(provider, idToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        assertThat(userRepository.findByProviderAndProviderId(provider, providerId))
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getId()).isEqualTo(result.getId()));

        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }

    @Test
    void idToken으로_기존_사용자_정보를_가져온다() {
        // given
        Provider provider = Provider.GOOGLE;
        String idToken = "test-id-token";
        String providerId = "google-existing-user";
        String email = "existing@example.com";
        String picture = "https://example.com/existing.jpg";

        // 기존 사용자 DB에 저장
        User existingUser = userRepository.save(new User(email, picture, provider, providerId));
        OauthUserInfo oauthUserInfo = new OauthUserInfo(providerId, email, picture);

        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken)).willReturn(oauthUserInfo);

        // when
        User result = sut.getUserInfo(provider, idToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUser.getId());

        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }

    @Test
    @DisplayName("ID 토큰 검증 중 예외가 발생하면 전파된다")
    void getUserInfo_VerificationFails_ThrowsException() {
        // given
        Provider provider = Provider.GOOGLE;
        String idToken = "invalid-id-token";

        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken))
                .willThrow(new IllegalArgumentException("ID토큰 검증에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo(provider, idToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID토큰 검증에 실패했습니다.");

        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }
}
