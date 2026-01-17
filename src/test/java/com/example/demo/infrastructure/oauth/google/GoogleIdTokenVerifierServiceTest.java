package com.example.demo.infrastructure.oauth.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.demo.application.dto.OauthUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenVerifierServiceTest {

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private GoogleIdToken googleIdToken;

    private GoogleIdTokenVerifierService googleIdTokenVerifierService;

    @BeforeEach
    void setUp() {
        googleIdTokenVerifierService = new GoogleIdTokenVerifierService("test-client-id");
        ReflectionTestUtils.setField(googleIdTokenVerifierService, "verifier", googleIdTokenVerifier);
    }

    @Test
    void 유효한_ID_토큰을_검증하고_OauthUserInfo를_반환한다() throws Exception {
        // given
        String idToken = "valid-id-token";
        Payload payload = new Payload();
        payload.setSubject("google-123456");
        payload.setEmail("test@example.com");
        payload.set("picture", "https://example.com/picture.jpg");
        payload.setIssuer("https://accounts.google.com");

        given(googleIdTokenVerifier.verify(idToken)).willReturn(googleIdToken);
        given(googleIdToken.getPayload()).willReturn(payload);

        // when
        OauthUserInfo oauthUserInfo = googleIdTokenVerifierService.verify(idToken);

        // then
        assertThat(oauthUserInfo).isNotNull();
        assertThat(oauthUserInfo.providerId()).isEqualTo("google-123456");
        assertThat(oauthUserInfo.email()).isEqualTo("test@example.com");
        verify(googleIdTokenVerifier).verify(idToken);
    }

    @Test
    void 검증_실패_시_null을_반환하면_예외를_던진다() throws Exception {
        // given
        String idToken = "invalid-id-token";
        given(googleIdTokenVerifier.verify(idToken)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> googleIdTokenVerifierService.verify(idToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID토큰 검증에 실패했습니다.")
            .cause()
            .hasMessage("유효하지 않은 ID토큰입니다.");

        verify(googleIdTokenVerifier).verify(idToken);
    }

    @Test
    void 유효하지_않은_발행자일_경우_예외를_던진다() throws Exception {
        // given
        String idToken = "valid-id-token";
        Payload payload = new Payload();
        payload.setSubject("google-123456");
        payload.setEmail("test@example.com");
        payload.setIssuer("https://invalid-issuer.com");

        given(googleIdTokenVerifier.verify(idToken)).willReturn(googleIdToken);
        given(googleIdToken.getPayload()).willReturn(payload);

        // when & then
        assertThatThrownBy(() -> googleIdTokenVerifierService.verify(idToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID토큰 검증에 실패했습니다.")
            .cause()
            .hasMessage("유효하지 않은 발행자: https://invalid-issuer.com");

        verify(googleIdTokenVerifier).verify(idToken);
    }

    @Test
    void 검증_중_예외가_발생하면_IllegalArgumentException을_던진다() throws Exception {
        // given
        String idToken = "invalid-id-token";
        given(googleIdTokenVerifier.verify(idToken))
            .willThrow(new RuntimeException("Verification failed"));

        // when & then
        assertThatThrownBy(() -> googleIdTokenVerifierService.verify(idToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID토큰 검증에 실패했습니다.")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(googleIdTokenVerifier).verify(idToken);
    }
}
