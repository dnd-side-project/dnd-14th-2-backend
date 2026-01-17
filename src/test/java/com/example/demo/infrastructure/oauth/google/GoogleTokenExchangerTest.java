package com.example.demo.infrastructure.oauth.google;

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

class GoogleTokenExchangerTest extends AbstractMockServerTest {

    private GoogleTokenExchanger googleTokenExchanger;
    private RestClient googleOauthRestClient;

    @BeforeEach
    void setUp() {
        googleOauthRestClient = anyRestClient();
        googleTokenExchanger = new GoogleTokenExchanger(
            googleOauthRestClient,
            new GoogleOauthProperties("test-client-id", "test-client-secret", "test")
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
        OauthToken result = googleTokenExchanger.exchange(authorizationCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.idToken()).isEqualTo("test-id-token");
        assertThat(result.accessToken()).isEqualTo("test-access-token");
        assertThat(result.refreshToken()).isEqualTo("test-refresh-token");

        RecordedRequest request = getMockWebServer().takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/x-www-form-urlencoded");

        String requestBody = request.getBody().utf8();
        assertThat(requestBody).contains("code=" + authorizationCode);
        assertThat(requestBody).contains("client_id=test-client-id");
        assertThat(requestBody).contains("client_secret=test-client-secret");
        assertThat(requestBody).contains("redirect_uri=test");
        assertThat(requestBody).contains("grant_type=authorization_code");
    }
}
