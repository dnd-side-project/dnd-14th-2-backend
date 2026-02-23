package com.example.demo.infrastructure.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.VerdictService;
import com.example.demo.application.dto.JurorVerdict;
import com.example.demo.application.dto.JurorVerdicts;
import com.example.demo.application.dto.LedgerEntryInfo;
import com.example.demo.application.dto.MyVerdict;
import com.example.demo.application.dto.MyVerdicts;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.application.exception.UnauthorizedException;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.VerdictType;
import com.example.demo.domain.enums.LedgerCategory;
import java.util.List;
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

@WebMvcTest(VerdictController.class)
@AutoConfigureRestDocs
@Tag("restdocs")
class VerdictDocumentationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    VerdictService verdictService;

    @MockitoBean
    TokenProvider tokenProvider;

    @Nested
    @DisplayName("소비 심판 요청")
    class RequestVerdict {

        @Test
        void requestVerdict_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

            // when & then
            mockMvc.perform(
                    post("/verdicts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"ledgerEntryId\": 1}")
                )
                .andExpect(status().isOk())
                .andDo(document("request-verdict",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .summary("소비 심판 요청")
                        .description("""
                            소비 내역에 대한 심판을 모든 친구들에게 요청합니다.
                            
                            - **[401]**
                              - **유효하지 않은 토큰(invalid-token)**
                                - 위조/변조/형식 오류 등 유효하지 않은 access token으로 요청한 경우
                                
                            - **[400]**
                              - **존재하지 않는 가계부 항목(non-exists-ledger-entry)**
                                - 소비 심판을 요청할 가계 항목이 존재하지 않을 경우
                            """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .requestSchema(Schema.schema("RequestJudgeWebRequest"))
                        .requestFields(
                            fieldWithPath("ledgerEntryId").type(NUMBER).description("심판 요청할 소비 내역 ID")
                        )
                        .build()
                    )
                ));

            verify(verdictService).requestVerdict(eq(1L), eq(userId));
        }

        @Test
        void fail_invalid_token_docs() throws Exception {
            // given
            String invalidToken = "invalid-token";
            given(tokenProvider.validateAccessToken(invalidToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    post("/verdicts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"ledgerEntryId\": 1}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("request-verdict - invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
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
        void requestVerdict_notFound_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."))
                .when(verdictService)
                .requestVerdict(eq(1L), eq(userId));

            // when & then
            mockMvc.perform(
                    post("/verdicts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"ledgerEntryId\": 1}")
                )
                .andExpect(status().isBadRequest())
                .andDo(document("request-verdict - non-exists-ledger-entry",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("발생 시각")
                        )
                        .build()
                    )
                ));
        }
    }

    @Nested
    @DisplayName("소비 심판 판결")
    class Judge {

        @Test
        void judge_docs() throws Exception {
            // given
            Long userId = 1L;
            Long verdictId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);

            // when & then
            mockMvc.perform(
                    patch("/verdicts/{id}", verdictId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"verdictType\": \"GUILTY\"}")
                )
                .andExpect(status().isOk())
                .andDo(document("judge-verdict",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("id").description("판결할 심판 ID")
                    ),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .summary("소비 심판 판결")
                        .description("""
                        배정된 소비 심판에 대해 판결합니다.
                        
                        - **[401]**
                          - **유효하지 않은 토큰(invalid-token)**
                            - 위조/변조/형식 오류 등 유효하지 않은 access token으로 요청한 경우
                            
                        - **[400]**
                          - **판결 권한 없음(no-permission)**
                            - 판결할 권한이 없는 경우 ex. 친구가 아니다.
                          - **이미 판결된 심판(already-judged)**
                            - 이미 판결되어진 심판에 대해 다시 판결할 경우
                        """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .pathParameters(
                            parameterWithName("id").description("판결할 심판 ID")
                        )
                        .requestSchema(Schema.schema("VerdictJudgeWebRequest"))
                        .requestFields(
                            fieldWithPath("verdictType").type(STRING).description("판결 결과 (GUILTY / NOT_GUILTY)")
                        )
                        .build()
                    )
                ));

            verify(verdictService).judge(eq(verdictId), eq(userId), eq(VerdictType.GUILTY));
        }

        @Test
        void fail_invalid_token_docs() throws Exception {
            // given
            Long verdictId = 1L;
            String invalidToken = "invalid-token";
            given(tokenProvider.validateAccessToken(invalidToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    patch("/verdicts/{id}", verdictId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"verdictType\": \"GUILTY\"}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("judge-verdict - invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("id").description("판결할 심판 ID")
                    ),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
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
        void judge_noPermission_docs() throws Exception {
            // given
            Long userId = 1L;
            Long verdictId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("판결 권한이 없습니다."))
                .when(verdictService)
                .judge(eq(verdictId), eq(userId), eq(VerdictType.GUILTY));

            // when & then
            mockMvc.perform(
                    patch("/verdicts/{id}", verdictId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"verdictType\": \"GUILTY\"}")
                )
                .andExpect(status().isBadRequest())
                .andDo(document("judge-verdict - no-permission",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("id").description("판결할 심판 ID")
                    ),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("발생 시각")
                        )
                        .build()
                    )
                ));
        }

        @Test
        void judge_alreadyCompleted_docs() throws Exception {
            // given
            Long userId = 1L;
            Long verdictId = 1L;
            String accessToken = "test-access-token";

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            doThrow(new IllegalArgumentException("이미 판결된 심판입니다."))
                .when(verdictService)
                .judge(eq(verdictId), eq(userId), eq(VerdictType.GUILTY));

            // when & then
            mockMvc.perform(
                    patch("/verdicts/{id}", verdictId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"verdictType\": \"GUILTY\"}")
                )
                .andExpect(status().isBadRequest())
                .andDo(document("judge-verdict - already-judged",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("id").description("판결할 심판 ID")
                    ),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("발생 시각")
                        )
                        .build()
                    )
                ));
        }
    }

    @Nested
    @DisplayName("내 소비 심판 목록 조회")
    class GetMyVerdicts {

        @Test
        void getMyVerdicts_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";

            MyVerdicts myVerdicts = new MyVerdicts(List.of(
                new MyVerdict(1L, new LedgerEntryInfo(1L, 7000L, LedgerCategory.FOOD, "커피"), VerdictType.GUILTY),
                new MyVerdict(2L, new LedgerEntryInfo(2L, 15000L, LedgerCategory.FOOD, "점심"), VerdictType.PENDING)
            ));

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            given(verdictService.getMyVerdicts(userId)).willReturn(myVerdicts);

            // when & then
            mockMvc.perform(
                    get("/verdicts/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("my-verdicts",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .summary("내 소비 심판 목록 조회")
                        .description("""
                            내가 요청한 소비 심판 목록을 조회합니다.
                            
                            - **[401]**
                              - **유효하지 않은 토큰(invalid-token)**
                                - 위조/변조/형식 오류 등 유효하지 않은 access token으로 요청한 경우
                            """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("MyVerdictsWebResponse"))
                        .responseFields(
                            fieldWithPath("verdicts").type(ARRAY).description("소비 심판 목록"),
                            fieldWithPath("verdicts[].id").type(NUMBER).description("심판 ID"),
                            fieldWithPath("verdicts[].ledgerEntryInfo.id").type(NUMBER).description("소비 내역 ID"),
                            fieldWithPath("verdicts[].ledgerEntryInfo.amount").type(NUMBER).description("소비 금액"),
                            fieldWithPath("verdicts[].ledgerEntryInfo.category").type(STRING)
                                .description("소비 카테고리"),
                            fieldWithPath("verdicts[].ledgerEntryInfo.description").type(STRING)
                                .description("소비 내용"),
                            fieldWithPath("verdicts[].verdictType").type(STRING).optional()
                                .description("판결 결과 (GUILTY / NOT_GUILTY / PENDING: 미판결)")
                        )
                        .build()
                    )
                ));

            verify(verdictService).getMyVerdicts(eq(userId));
        }

        @Test
        void fail_invalid_token_docs() throws Exception {
            // given
            String invalidToken = "invalid-token";
            given(tokenProvider.validateAccessToken(invalidToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    get("/verdicts/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("my-verdicts - invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
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
    }

    @Nested
    @DisplayName("내가 판결해야 할 소비 심판 목록 조회")
    class GetJurorVerdicts {

        @Test
        void getJurorVerdicts_docs() throws Exception {
            // given
            Long userId = 1L;
            String accessToken = "test-access-token";

            JurorVerdicts jurorVerdicts = new JurorVerdicts(List.of(
                new JurorVerdict(1L, new UserInfo(2L, "토끼abc", 1, "profile.jpg"),
                    new LedgerEntryInfo(1L, 7000L, LedgerCategory.FOOD, "커피"), VerdictType.GUILTY),
                new JurorVerdict(2L, new UserInfo(3L, "강아지123", 2, "profile2.jpg"),
                    new LedgerEntryInfo(2L, 15000L, LedgerCategory.FOOD, "점심"), VerdictType.PENDING)
            ));

            given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
            given(verdictService.getJurorVerdicts(userId)).willReturn(jurorVerdicts);

            // when & then
            mockMvc.perform(
                    get("/verdicts/juror")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("juror-verdicts",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
                        .summary("내가 판결해야 할 소비 심판 목록 조회")
                        .description("""
                        내가 배심원으로 배정된 소비 심판 목록을 조회합니다.
                        
                        - **[401]**
                          - **유효하지 않은 토큰(invalid-token)**
                            - 위조/변조/형식 오류 등 유효하지 않은 access token으로 요청한 경우
                        """)
                        .requestHeaders(
                            headerWithName(HttpHeaders.AUTHORIZATION).description("pickle의 access token")
                        )
                        .responseSchema(Schema.schema("JurorVerdictsWebResponse"))
                        .responseFields(
                            fieldWithPath("jurorVerdicts").type(ARRAY).description("소비 심판 목록"),
                            fieldWithPath("jurorVerdicts[].id").type(NUMBER).description("심판 ID"),
                            fieldWithPath("jurorVerdicts[].defendantInfo.id").type(NUMBER).description("피고 유저 ID"),
                            fieldWithPath("jurorVerdicts[].defendantInfo.nickname").type(STRING).description("피고 닉네임"),
                            fieldWithPath("jurorVerdicts[].defendantInfo.level").type(NUMBER).description("피고 레벨"),
                            fieldWithPath("jurorVerdicts[].ledgerEntryInfo.id").type(NUMBER).description("소비 내역 ID"),
                            fieldWithPath("jurorVerdicts[].ledgerEntryInfo.amount").type(NUMBER).description("소비 금액"),
                            fieldWithPath("jurorVerdicts[].ledgerEntryInfo.category").type(STRING)
                                .description("소비 카테고리"),
                            fieldWithPath("jurorVerdicts[].ledgerEntryInfo.description").type(STRING)
                                .description("소비 내용"),
                            fieldWithPath("jurorVerdicts[].verdictType").type(STRING).optional()
                                .description("판결 결과 (GUILTY / NOT_GUILTY / PENDING: 미판결)")
                        )
                        .build()
                    )
                ));

            verify(verdictService).getJurorVerdicts(eq(userId));
        }

        @Test
        void fail_invalid_token_docs() throws Exception {
            // given
            String invalidToken = "invalid-token";
            given(tokenProvider.validateAccessToken(invalidToken))
                .willThrow(new UnauthorizedException("유효하지 않은 토큰 정보입니다."));

            // when & then
            mockMvc.perform(
                    get("/verdicts/juror")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 정보입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("juror-verdicts - invalid-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Verdict")
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
    }
}
