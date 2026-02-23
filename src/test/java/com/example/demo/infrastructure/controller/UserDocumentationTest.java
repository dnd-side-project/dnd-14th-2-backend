package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.application.exception.UnauthorizedException;
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

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    class ChangeNickname {

        @Test
        void changeNickname_docs() throws Exception {
            // given
            Long userId = 1L;
            String nickname = "name";
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            given(userService.changeNickname(userId, nickname)).willReturn(nickname);

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
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andDo(document("change-nickname",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("닉네임 변경")
                        .description("""
                            로그인한 사용자의 닉네임을 변경합니다.
                            
                            - **[400]**
                              - **존재하지 않는 사용자(non-exists-user)**
                                - access token 속 사용자 정보가 존재하지 않는 경우
                              - **중복되는 닉네임(duplicate-nickname)**
                                - 새로운 닉네임이 기존에 존재하는 닉네임과 중복되는 경우
                              - **비어있는 닉네임(empty-nickname)**
                                - 새로운 닉네임이 비어있는 경우
                              - **닉네임 최대 길이 초과(exceed-max-length)**
                                - 새로운 닉네임의 길이가 제한을 초과했을 경우
                              - **형식 불일치(wrong-format)**
                                - 새로운 닉네임의 형식이 맞지 않을 경우
                            """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .requestSchema(Schema.schema("NicknameWebRequest"))
                        .responseSchema(Schema.schema("NicknameWebResponse"))
                        .requestFields(
                            fieldWithPath("nickname").type(STRING).description("사용자 입력 닉네임")
                        )
                        .responseFields(
                            fieldWithPath("nickname").type(STRING).description("사용자의 새로운 닉네임")
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
                .andDo(document("change-nickname - non-exists-user",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
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
                .andDo(document("change-nickname - duplicate-nickname",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
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
                .andDo(document("change-nickname - empty-nickname",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
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
                .andDo(document("change-nickname - exceed-max-length",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
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
                .andDo(document("change-nickname - wrong-format",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
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
    class Withdraw {

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
                .andDo(document("withdraw",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("회원 탈퇴")
                        .description("사용자를 탈퇴합니다. (멱등: 이미 탈퇴/존재하지 않아도 204)")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .build()
                    )
                ));

            // 서비스 호출 검증
            verify(userService).withdrawUser(eq(userId));
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회")
    class GetUserInfo {

        @Test
        void getUserInfo_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";
            UserInfo userInfo = new UserInfo(userId, "토끼abc", 1, "profile.jpg");

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            given(userService.getUserInfo(userId)).willReturn(userInfo);

            // when & then
            mockMvc.perform(
                    get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("토끼abc"))
                .andExpect(jsonPath("$.level").value(1))
                .andDo(document("get-user-info",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("사용자 정보 조회")
                        .description("""
                            로그인한 사용자의 정보를 조회합니다.
                            
                            - **[401]**
                              - **만료된 토큰(expired-token)**
                                - 만료된 access token으로 요청한 경우
                              - **유효하지 않은 토큰(invalid-token)**
                                - 위조/변조/형식 오류 등 유효하지 않은 access token으로 요청한 경우
                            
                            - **[400]**
                              - **존재하지 않는 사용자(non-exists-user)**
                                - 로그인한 사용자의 정보를 조회할 때, 사용자 정보가 존재하지 않아 실패한 경우
                            """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("UserInfoWebResponse"))
                        .responseFields(
                            fieldWithPath("id").type(NUMBER).description("사용자의 id"),
                            fieldWithPath("nickname").type(STRING).description("사용자 닉네임 (설정하지 않은 경우 랜덤 닉네임)"),
                            fieldWithPath("level").type(NUMBER).description("사용자 레벨 (기본값: 0)")
                        )
                        .build()
                    )
                ));

            verify(userService).getUserInfo(eq(userId));
        }

        @Test
        void fail_expired_token_docs() throws Exception {
            // given
            String expiredToken = "expired-token";
            given(tokenProvider.validateAccessToken(expiredToken))
                .willThrow(new UnauthorizedException("만료된 토큰입니다."));

            // when & then
            mockMvc.perform(
                    get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("만료된 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("get-user-info - expired-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
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
        void fail_invalid_token_docs() throws Exception {
            // given
            String invalidToken = "invalid-token";
            given(tokenProvider.validateAccessToken(invalidToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("get-user-info - invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
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
        void getUserInfo_fail_not_exists_user_docs() throws Exception {
            // given
            Long userId = 999L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."))
                .when(userService)
                .getUserInfo(userId);

            // when & then
            mockMvc.perform(
                    get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("get-user-info - non-exists-user",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(userService).getUserInfo(eq(userId));
        }
    }
}
