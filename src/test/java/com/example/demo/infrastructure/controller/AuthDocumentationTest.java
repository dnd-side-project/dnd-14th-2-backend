package com.example.demo.infrastructure.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
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
                    .build()
                )
            ));
        then(authService).should().logout(userId);
    }

    @Nested
    @DisplayName("토큰 재발행")
    class reissue {
        @Test
        void reissue_docs() throws Exception {
            // given
            long userId = 1L;
            String refreshToken = "jwt.refresh.token";
            String newAccessToken = "jwt.new.access.token";
            String newRefreshToken = "jwt.new.refresh.token";

            given(tokenProvider.validateRefreshToken(refreshToken)).willReturn(userId);
            given(authService.reissueToken(refreshToken)).willReturn(new TokenResponse(newAccessToken, newRefreshToken));

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
                .andDo(document("reissue",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
                        .summary("액세스 토큰 재발급")
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
                .andDo(document("reissue-fail-invalid-refresh-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
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
                .andDo(document("reissue-fail-expired-refresh-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
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
                .andDo(document("reissue-fail-token-type",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
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
                .andDo(document("reissue-fail-unauthorized",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Auth")
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
