package com.example.demo.application;

import com.example.demo.application.dto.OauthToken;
import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.oauth.IdTokenVerifier;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.application.oauth.TokenExchanger;
import com.example.demo.application.oauth.TokenExchangerFactory;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class OauthServiceTest extends AbstractIntegrationTest {

    @MockitoBean
    private TokenExchangerFactory exchangerFactory;

    @MockitoBean
    private IdTokenVerifier OidcIdTokenVerifierService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthService sut;


    @Test
    void 인가_코드로_새로운_사용자_정보를_가져와_저장한다() {
        // given
        Provider provider = Provider.GOOGLE;
        String authorizationCode = "test-authorization-code";
        String idToken = "test-id-token";
        String providerId = "google-123456";
        String email = "test@example.com";
        String picture = "https://example.com/picture.jpg";

        TokenExchanger tokenExchanger = Mockito.mock(TokenExchanger.class);
        OauthToken oauthToken = new OauthToken(idToken, "access-token", "refresh-token");
        OauthUserInfo oauthUserInfo = new OauthUserInfo(providerId, email, picture);

        given(exchangerFactory.get(provider)).willReturn(tokenExchanger);
        given(tokenExchanger.exchange(authorizationCode)).willReturn(oauthToken);
        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken)).willReturn(oauthUserInfo);

        // when
        User result = sut.getUserInfo(provider, authorizationCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        assertThat(userRepository.findByProviderAndProviderId(provider, providerId))
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getId()).isEqualTo(result.getId()));

        verify(exchangerFactory).get(provider);
        verify(tokenExchanger).exchange(authorizationCode);
        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }

    @Test
    void 인가_코드로_기존_사용자_정보를_가져온다() {
        // given
        Provider provider = Provider.GOOGLE;
        String authorizationCode = "test-authorization-code";
        String idToken = "test-id-token";
        String providerId = "google-existing-user";
        String email = "existing@example.com";
        String picture = "https://example.com/existing.jpg";

        // 기존 사용자 DB에 저장
        User existingUser = userRepository.save(new User(email, picture, provider, providerId));

        TokenExchanger tokenExchanger = Mockito.mock(TokenExchanger.class);
        OauthToken oauthToken = new OauthToken(idToken, "access-token", "refresh-token");
        OauthUserInfo oauthUserInfo = new OauthUserInfo(providerId, email, picture);

        given(exchangerFactory.get(provider)).willReturn(tokenExchanger);
        given(tokenExchanger.exchange(authorizationCode)).willReturn(oauthToken);
        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken)).willReturn(oauthUserInfo);

        // when
        User result = sut.getUserInfo(provider, authorizationCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUser.getId());

        verify(exchangerFactory).get(provider);
        verify(tokenExchanger).exchange(authorizationCode);
        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }

    @Test
    void OauthToken이_null일_때_예외를_던진다() {
        // given
        Provider provider = Provider.GOOGLE;
        String authorizationCode = "test-authorization-code";

        TokenExchanger tokenExchanger = Mockito.mock(TokenExchanger.class);

        given(exchangerFactory.get(provider)).willReturn(tokenExchanger);
        given(tokenExchanger.exchange(authorizationCode)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo(provider, authorizationCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인가 코드를 토큰으로 교환하는데 실패했습니다. (id_token을 찾을 수 없음)");

        verify(exchangerFactory).get(provider);
        verify(tokenExchanger).exchange(authorizationCode);
        verify(OidcIdTokenVerifierService, never()).verifyAndGetUserInfo(any(), anyString());
    }

    @Test
    void idToken이_null일_때_예외를_던진다() {
        // given
        Provider provider = Provider.GOOGLE;
        String authorizationCode = "test-authorization-code";

        TokenExchanger tokenExchanger = Mockito.mock(TokenExchanger.class);
        OauthToken oauthToken = new OauthToken(null, "access-token", "refresh-token");

        given(exchangerFactory.get(provider)).willReturn(tokenExchanger);
        given(tokenExchanger.exchange(authorizationCode)).willReturn(oauthToken);

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo(provider, authorizationCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인가 코드를 토큰으로 교환하는데 실패했습니다. (id_token을 찾을 수 없음)");

        verify(exchangerFactory).get(provider);
        verify(tokenExchanger).exchange(authorizationCode);
        verify(OidcIdTokenVerifierService, never()).verifyAndGetUserInfo(any(), anyString());
    }

    @Test
    @DisplayName("ID 토큰 검증 중 예외가 발생하면 전파된다")
    void getUserInfo_VerificationFails_ThrowsException() {
        // given
        Provider provider = Provider.GOOGLE;
        String authorizationCode = "test-authorization-code";
        String idToken = "invalid-id-token";

        TokenExchanger tokenExchanger = Mockito.mock(TokenExchanger.class);
        OauthToken oauthToken = new OauthToken(idToken, "access-token", "refresh-token");

        given(exchangerFactory.get(provider)).willReturn(tokenExchanger);
        given(tokenExchanger.exchange(authorizationCode)).willReturn(oauthToken);
        given(OidcIdTokenVerifierService.verifyAndGetUserInfo(provider, idToken))
                .willThrow(new IllegalArgumentException("ID토큰 검증에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo(provider, authorizationCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID토큰 검증에 실패했습니다.");

        verify(exchangerFactory).get(provider);
        verify(tokenExchanger).exchange(authorizationCode);
        verify(OidcIdTokenVerifierService).verifyAndGetUserInfo(provider, idToken);
    }
}
