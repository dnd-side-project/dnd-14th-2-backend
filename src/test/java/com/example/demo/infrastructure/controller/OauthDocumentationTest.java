package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.application.oauth.OauthServiceFactory;
import com.example.demo.infrastructure.controller.dto.OauthLoginWebRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OauthController.class)
@AutoConfigureRestDocs
class OauthDocumentationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OauthServiceFactory oauthServiceFactory;

    @MockitoBean
    private OauthService kakaoOauthService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    public void oauthLogin_kakao_docs() throws Exception {
        // given
        String code = "test-authorization-code";
        User user = new User(
                "test@email.com",
                "https://profile/image.jpg",
                Provider.KAKAO,
                "kakao-provider-id"
        );
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";
        given(oauthServiceFactory.get(Provider.KAKAO)).willReturn(kakaoOauthService);
        given(kakaoOauthService.getUserInfo(code)).willReturn(user);
        given(authService.issueTokens(user)).willReturn(new TokenResponse(accessToken, refreshToken));

        // when & then
        mockMvc.perform(
                        post("/oauth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"provider\":\"KAKAO\",\"code\":\"" + code + "\"}")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("jwt.refresh.token"))
                .andDo(document("oauth-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("OAuth")
                                .summary("소셜 로그인")
                                .requestFields(
                                        fieldWithPath("provider").type(STRING).description("소셜 로그인 제공자(예: KAKAO, GOOGLE)"),
                                        fieldWithPath("code").type(STRING).description("OAuth 인가 코드(authorization code)")
                                )
                                .responseFields(
                                        fieldWithPath("accessToken").type(STRING).description("PICKLE access token(JWT)"),
                                        fieldWithPath("refreshToken").type(STRING).description("PICKLE refresh token(JWT)")
                                )
                                .build()
                        )
                ));
    }
}