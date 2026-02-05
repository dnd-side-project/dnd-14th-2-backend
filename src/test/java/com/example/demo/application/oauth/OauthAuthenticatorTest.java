package com.example.demo.application.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.domain.Provider;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class OauthAuthenticatorTest extends AbstractIntegrationTest {

    @MockitoBean
    private IdTokenVerifier idTokenVerifier;

    @Autowired
    private OauthAuthenticator sut;


    @Test
    void idToken으로_사용자_정보를_가져온다() {
        // given
        Provider provider = Provider.GOOGLE;
        String idToken = "test-id-token";
        String providerId = "google-123456";
        String email = "test@example.com";
        String picture = "https://example.com/picture.jpg";

        OauthUserInfo oauthUserInfo = new OauthUserInfo(providerId, email, picture);
        given(idTokenVerifier.verifyAndGetUserInfo(provider, idToken)).willReturn(oauthUserInfo);

        // when
        OauthUserInfo result = sut.authenticate(provider, idToken);

        // then
        assertThat(result).isNotNull();
        verify(idTokenVerifier).verifyAndGetUserInfo(provider, idToken);
    }

    @Test
    @DisplayName("ID 토큰 검증 중 예외가 발생하면 전파된다")
    void authenticate_VerificationFails_ThrowsException() {
        // given
        Provider provider = Provider.GOOGLE;
        String idToken = "invalid-id-token";

        given(idTokenVerifier.verifyAndGetUserInfo(provider, idToken))
                .willThrow(new IllegalArgumentException("ID토큰 검증에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> sut.authenticate(provider, idToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID토큰 검증에 실패했습니다.");

        verify(idTokenVerifier).verifyAndGetUserInfo(provider, idToken);
    }
}
