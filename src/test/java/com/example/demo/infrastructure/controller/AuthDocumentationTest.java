package com.example.demo.infrastructure.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.exception.UnauthorizedException;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthAuthenticator;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureRestDocs
@Tag("restdocs")
class AuthDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OauthAuthenticator oauthAuthenticator;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    void oauthLogin_docs() throws Exception {
        // given
        String idToken = "test-id-token";
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";

        given(authService.login(Provider.KAKAO, idToken)).willReturn(new TokenResponse(accessToken, refreshToken));

        // when & then
        mockMvc.perform(
                post("/oauth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"provider\":\"KAKAO\",\"idToken\":\"" + idToken + "\"}")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value(accessToken))
            .andExpect(jsonPath("$.refreshToken").value(refreshToken))
            .andDo(document("oauth-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("소셜 로그인")
                    .description("카카오/구글을 통해 소셜 로그인 할 수 있습니다.")
                    .requestSchema(Schema.schema("OauthLoginWebRequest"))
                    .responseSchema(Schema.schema("AuthTokenWebResponse"))
                    .requestFields(
                        fieldWithPath("provider").type(STRING).description("소셜 로그인 제공자(예: KAKAO, GOOGLE)"),
                        fieldWithPath("idToken").type(STRING).description("OIDC ID Token")
                    )
                    .responseFields(
                        fieldWithPath("accessToken").type(STRING).description("PICKLE access token(JWT)"),
                        fieldWithPath("refreshToken").type(STRING).description("PICKLE refresh token(JWT)")
                    )
                    .build()
                )
            ));
    }

    @Test
    void oauthDemo_docs() throws Exception {
        // given
        String deviceId = "test-device-id";
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";

        given(authService.loginDemo(deviceId)).willReturn(new TokenResponse(accessToken, refreshToken));

        // when & then
        mockMvc.perform(
                post("/oauth/demo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"deviceId\":\"" + deviceId + "\"}")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value(accessToken))
            .andExpect(jsonPath("$.refreshToken").value(refreshToken))
            .andDo(document("oauth-demo",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("데모 로그인")
                    .description("deviceId를 통해 데모 로그인을 할 수 있습니다.")
                    .requestSchema(Schema.schema("DemoLoginWebRequest"))
                    .responseSchema(Schema.schema("AuthTokenWebResponse"))
                    .requestFields(
                        fieldWithPath("deviceId").type(STRING).description("기기 고유 ID")
                    )
                    .responseFields(
                        fieldWithPath("accessToken").type(STRING).description("PICKLE access token(JWT)"),
                        fieldWithPath("refreshToken").type(STRING).description("PICKLE refresh token(JWT)")
                    )
                    .build()
                )
            ));
    }

    @Test
    void logout_docs() throws Exception {
        // given
        long userId = 1L;
        String accessToken = "jwt.access.token";

        given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

        // when & then
        mockMvc.perform(
                post("/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andDo(document("logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("로그아웃")
                    .description("기존 회원의 로그아웃 기능입니다.")
                    .requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                    )
                    .build()
                )
            ));
        then(authService).should().logout(userId);
    }

    @Nested
    @DisplayName("토큰 재발행")
    class Reissue {
        @Test
        void reissue_docs() throws Exception {
            // given
            String refreshToken = "jwt.refresh.token";
            String newAccessToken = "jwt.new.access.token";
            String newRefreshToken = "jwt.new.refresh.token";

            given(authService.reissueToken(refreshToken))
                .willReturn(new TokenResponse(newAccessToken, newRefreshToken));

            // when & then
            mockMvc.perform(
                    post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken))
                .andDo(document("token-reissue",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .summary("액세스 토큰 재발급")
                        .description("""
                            만료된 access token을 refresh token을 통해 재발급 합니다.
                            
                            - **[401]**
                              - **유효하지 않은 토큰(invalid-token)**
                                - 유효하지 않은 리프레시 토큰으로 시도했을 경우
                              - **만료된 토큰(expired-token)**
                                - 만료된 토큰으로 시도했을 경우
                              - **토큰 타입 불일치(unmatch-token-type)**
                                - 리프레시 토큰이 아닌 다른 토큰을 사용했을 경우
                              - **인증되지 않은 사용자(unauthorized-user)**
                                - 리프레시 토큰 인증에 실패했을 경우 ex. 다른 서버의 리프레시 토큰
                            """)
                        .requestSchema(Schema.schema("ReissueTokenWebRequest"))
                        .responseSchema(Schema.schema("AuthTokenWebResponse"))
                        .requestFields(
                            fieldWithPath("refreshToken").type(STRING).description("사용자의 refresh token(JWT)")
                        )
                        .responseFields(
                            fieldWithPath("accessToken").type(STRING).description("새로운 access token(JWT)"),
                            fieldWithPath("refreshToken").type(STRING).description("새로운 refresh token(JWT)")
                        )
                        .build()
                    )
                ));
            then(authService).should().reissueToken(refreshToken);
        }

        @Test
        void reissue_fail_invalid_refresh_token() throws Exception {
            // given
            String refreshToken = "invalid.refresh.token";

            given(authService.reissueToken(refreshToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andDo(document("token-reissue_invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .requestSchema(Schema.schema("ReissueTokenWebRequest"))
                        .requestFields(
                            fieldWithPath("refreshToken").type(STRING).description("사용자의 refresh token(JWT)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            then(authService).should().reissueToken(refreshToken);
        }

        @Test
        void reissue_fail_expired_refresh_token() throws Exception {
            // given
            String refreshToken = "expired.refresh.token";

            given(authService.reissueToken(refreshToken))
                .willThrow(new UnauthorizedException("만료된 토큰입니다."));

            // when & then
            mockMvc.perform(
                    post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("만료된 토큰입니다."))
                .andDo(document("token-reissue_expired-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .requestSchema(Schema.schema("ReissueTokenWebRequest"))
                        .requestFields(
                            fieldWithPath("refreshToken").type(STRING).description("사용자의 refresh token(JWT)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            then(authService).should().reissueToken(refreshToken);
        }

        @Test
        void reissue_fail_token_type() throws Exception {
            // given
            String accessToken = "jwt.access.token";

            given(authService.reissueToken(accessToken))
                .willThrow(new UnauthorizedException("잘못된 토큰 타입입니다."));

            // when & then
            mockMvc.perform(
                    post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + accessToken + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("잘못된 토큰 타입입니다."))
                .andDo(document("token-reissue_unmatch-token-type",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .requestSchema(Schema.schema("ReissueTokenWebRequest"))
                        .requestFields(
                            fieldWithPath("refreshToken").type(STRING).description("사용자의 refresh token(JWT)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            then(authService).should().reissueToken(accessToken);
        }

        @Test
        void reissue_fail_unauthorized() throws Exception {
            // given
            String refreshToken = "another.refresh.token";

            given(authService.reissueToken(refreshToken))
                .willThrow(new UnauthorizedException("인증되지 않은 사용자입니다."));

            // when & then
            mockMvc.perform(
                    post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."))
                .andDo(document("token-reissue_unauthorized-user",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .requestSchema(Schema.schema("ReissueTokenWebRequest"))
                        .requestFields(
                            fieldWithPath("refreshToken").type(STRING).description("사용자의 refresh token(JWT)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            then(authService).should().reissueToken(refreshToken);
        }
    }
}
