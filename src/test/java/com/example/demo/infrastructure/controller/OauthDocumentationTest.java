package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.application.oauth.*;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
    private OauthService oauthService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    void oauthLogin_docs() throws Exception {
        // given
        String idToken = "test-id-token";
        User user = new User(
                "test@email.com",
                "https://profile/image.jpg",
                Provider.KAKAO,
                "provider-id"
        );
        String accessToken = "jwt.access.token";
        String refreshToken = "jwt.refresh.token";

        given(oauthService.getUserInfo(Provider.KAKAO, idToken)).willReturn(user);
        given(authService.issueTokens(user)).willReturn(new TokenResponse(accessToken, refreshToken));

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
                                .tag("OAuth")
                                .summary("소셜 로그인")
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

        given(tokenProvider.validateToken(accessToken)).willReturn(userId);

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
                    .requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                    )
                    .build()
                )
            ));
        then(authService).should().logout(userId);
    }
}
