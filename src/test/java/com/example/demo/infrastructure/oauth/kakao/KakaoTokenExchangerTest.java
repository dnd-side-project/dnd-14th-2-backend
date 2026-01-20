package com.example.demo.infrastructure.oauth.kakao;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.application.dto.OauthToken;
import com.example.demo.util.AbstractMockServerTest;
import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

class KakaoTokenExchangerTest extends AbstractMockServerTest {

    private KakaoTokenExchanger kakaoTokenExchanger;
    private RestClient kakaoTokenRestClient;

    @BeforeEach
    void setUp() {
        kakaoTokenRestClient = anyRestClient();
        kakaoTokenExchanger = new KakaoTokenExchanger(
                kakaoTokenRestClient,
                new KakaoOauthProperties("test-client-id", "test-client-secret", "test-redirect-uri")
        );
    }

    @Test
    void 인가_코드를_토큰으로_정상적으로_교환한다() throws InterruptedException {
        // given
        String authorizationCode = "test-authorization-code";
        String responseBody = """
            {
                "id_token": "test-id-token",
                "access_token": "test-access-token",
                "refresh_token": "test-refresh-token"
            }
            """;

        getMockWebServer().enqueue(new MockResponse.Builder()
                .code(200)
                .body(responseBody)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build());

        // when
        OauthToken result = kakaoTokenExchanger.exchange(authorizationCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.idToken()).isEqualTo("test-id-token");
        assertThat(result.accessToken()).isEqualTo("test-access-token");
        assertThat(result.refreshToken()).isEqualTo("test-refresh-token");

        RecordedRequest request = getMockWebServer().takeRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/x-www-form-urlencoded");

        // RestClient가 charset 등을 붙일 수 있어서 contains로 체크(구글 테스트는 equals였지만 카카오는 더 안전하게)
        assertThat(request.getHeaders().get("Content-Type")).contains("application/x-www-form-urlencoded");
        assertThat(request.getHeaders().get("Accept")).contains("application/json");

        String requestBody = request.getBody().utf8();
        assertThat(requestBody).contains("grant_type=authorization_code");
        assertThat(requestBody).contains("client_id=test-client-id");
        assertThat(requestBody).contains("redirect_uri=test-redirect-uri");
        assertThat(requestBody).contains("code=" + authorizationCode);
        assertThat(requestBody).contains("client_secret=test-client-secret");
    }
}