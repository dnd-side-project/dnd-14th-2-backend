package com.example.demo.infrastructure.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.demo.application.UserService;
import com.example.demo.application.oauth.TokenProvider;
import org.junit.jupiter.api.Tag;
import com.example.demo.application.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
@Tag("restdocs")
class UserDocumentationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    TokenProvider tokenProvider;

    @Test
    void registerNickname_docs() throws Exception {
        // given
        Long userId = 1L;
        String nickname = "name";
        String invitationCode = "test-invitation-code";
        String accessToken = "test-access-token";

        given(userService.registerNickname(userId, nickname)).willReturn(invitationCode);
        given(tokenProvider.validateToken(accessToken)).willReturn(userId);

        // when & then
        mockMvc.perform(
                post("/users/me/nickname")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nickname\":\"" + nickname + "\"}")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.invitationCode").value(invitationCode))
            .andDo(document("register-nickname",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("닉네임 등록")
                    .description("로그인한 사용자의 닉네임을 등록하고 초대코드를 반환합니다.")
                    .requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                    )
                    .requestFields(
                        fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                    )
                    .responseFields(
                        fieldWithPath("invitationCode").type(STRING).description("해당 사용자의 초대코드")
                    )
                    .build()
                )
            ));
    }

    @Test
    void withdrawUser_docs() throws Exception {
        // given
        Long userId = 1L;
        String accessToken = "test-access-token";

        given(tokenProvider.validateToken(accessToken)).willReturn(userId);

        // when & then
        mockMvc.perform(
                delete("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andDo(document("withdraw-user",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 탈퇴")
                    .description("사용자를 탈퇴합니다. (멱등: 이미 탈퇴/존재하지 않아도 204)")
                    .build()
                )
            ));

        // 서비스 호출 검증
        verify(userService).withdrawUser(eq(userId));
    }
}
