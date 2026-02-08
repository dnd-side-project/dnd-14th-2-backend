package com.example.demo.infrastructure.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.application.user.UserService;
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

    @Nested
    @DisplayName("닉네임 변경")
    class changeNickname {

        @Test
        void changeNickname_docs() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "name";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andDo(document("닉네임 변경",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("닉네임 변경")
                        .description("로그인한 사용자의 닉네임을 변경합니다.")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .build()
                    )
                ));

            verify(userService).changeNickname(eq(userId), eq(nickname));
        }

        @Test
        void changeNickname_fail_not_exists_user() throws Exception {
            // given
            Long userId = 999L;
            String nickname = "name";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."))
                .when(userService)
                .changeNickname(userId, nickname);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
                .andDo(document("닉네임 변경 - 존재하지 않는 사용자",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(userService).changeNickname(eq(userId), eq(nickname));
        }

        @Test
        void changeNickname_fail_exists() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "name";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("중복되는 닉네임입니다."))
                .when(userService)
                .changeNickname(userId, nickname);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("중복되는 닉네임입니다."))
                .andDo(document("닉네임 변경 - 중복 닉네임",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(userService).changeNickname(eq(userId), eq(nickname));
        }

        @Test
        void changeNickname_fail_blank() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("닉네임은 비어있을 수 없습니다."))
                .andDo(document("닉네임 변경 - 비어있는 닉네임",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));
        }

        @Test
        void changeNickname_fail_over_max_length() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "name1234567";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("닉네임은 5자 이내여야 합니다."))
                .when(userService)
                .changeNickname(userId, nickname);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("닉네임은 5자 이내여야 합니다."))
                .andDo(document("닉네임 변경 - 닉네임 최대 길이 초과",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(userService).changeNickname(eq(userId), eq(nickname));
        }

        @Test
        void changeNickname_fail_invalid() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "NAME$";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("닉네임은 한글, 숫자, 영어 소문자로만 이루어져야 합니다."))
                .when(userService)
                .changeNickname(userId, nickname);

            // when & then
            mockMvc.perform(
                    post("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("닉네임은 한글, 숫자, 영어 소문자로만 이루어져야 합니다."))
                .andDo(document("닉네임 변경 - 형식 불일치",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(userService).changeNickname(eq(userId), eq(nickname));
        }
    }

    @Nested
    @DisplayName("회원탈퇴")
    class withdraw {

        @Test
        void withdrawUser_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

            // when & then
            mockMvc.perform(
                    delete("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andDo(document("회원탈퇴",
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
}
