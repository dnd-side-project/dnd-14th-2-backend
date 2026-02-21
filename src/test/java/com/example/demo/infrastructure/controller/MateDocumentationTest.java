package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.MateService;
import com.example.demo.application.dto.MateInfo;
import com.example.demo.application.dto.MateReceivedInfo;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.domain.enums.MateStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.demo.util.RestDocsUtils.allowedValues;
import static com.example.demo.util.RestDocsUtils.enumList;
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
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateController.class)
@AutoConfigureRestDocs
@Tag("restdocs")
class MateDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateService mateService;

    @MockitoBean
    private TokenProvider tokenProvider;

    private final String accessToken = "jwt.access.token";

    @BeforeEach
    void setUpAuth() {
        final long userId = 1L;
        given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
    }

    @Nested
    @DisplayName("친구 요청 보내기")
    class RequestMate {

        @Test
        void request_mate_docs() throws Exception {
            // given
            String invitationCode = "ABCDEF";
            given(mateService.requestMate(eq(1L), eq(invitationCode))).willReturn(1L);

            // when & then
            mockMvc.perform(
                    post("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"" + invitationCode + "\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/mates/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mateId").value(1))
                .andDo(document("친구 요청 보내기",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .summary("친구 요청 보내기")
                        .description("상대방의 초대코드를 이용하여 친구 요청을 보냅니다.")
                        .requestSchema(Schema.schema("CreateMateWebRequest"))
                        .responseSchema(Schema.schema("MateCreateWebResponse"))
                        .requestFields(
                            fieldWithPath("invitationCode").type(STRING)
                                .attributes(
                                    key("example").value("ABCDEF"),
                                    key("format").value("영문 대문자 6자리")
                                )
                                .description("상대방의 초대코드 (영문 대문자 6자리)")
                        )
                        .responseFields(
                            fieldWithPath("mateId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("생성된 친구 요청 ID")
                        )
                        .build()
                    )
                ));

            verify(mateService).requestMate(eq(1L), eq(invitationCode));
        }

        @Test
        void request_mate_fail_not_exists_invitation_code_docs() throws Exception {
            // given
            String invitationCode = "ZZZZZZ";
            given(mateService.requestMate(eq(1L), eq(invitationCode)))
                .willThrow(new IllegalArgumentException("존재하지 않는 초대코드입니다."));

            // when & then
            mockMvc.perform(
                    post("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"" + invitationCode + "\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 초대코드입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 보내기 - 존재하지 않는 초대코드 (초대코드에 해당하는 사용자가 존재하지 않는 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .requestSchema(Schema.schema("CreateMateWebRequest"))
                        .requestFields(
                            fieldWithPath("invitationCode").type(STRING).description("상대방의 초대코드")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(mateService).requestMate(eq(1L), eq(invitationCode));
        }

        @Test
        void request_mate_fail_already_exists_docs() throws Exception {
            // given
            String invitationCode = "ABCDEF";
            given(mateService.requestMate(eq(1L), eq(invitationCode)))
                .willThrow(new IllegalArgumentException("이미 친구 관계가 존재하거나 요청 대기 중입니다."));

            // when & then
            mockMvc.perform(
                    post("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"" + invitationCode + "\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 친구 관계가 존재하거나 요청 대기 중입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 보내기 - 이미 관계 존재 (이미 친구이거나 대기 중인 요청이 있는 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .requestSchema(Schema.schema("CreateMateWebRequest"))
                        .requestFields(
                            fieldWithPath("invitationCode").type(STRING).description("상대방의 초대코드")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(mateService).requestMate(eq(1L), eq(invitationCode));
        }

        @Test
        void request_mate_fail_self_request_docs() throws Exception {
            // given
            String invitationCode = "ABCDEF";
            given(mateService.requestMate(eq(1L), eq(invitationCode)))
                .willThrow(new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다."));

            // when & then
            mockMvc.perform(
                    post("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"" + invitationCode + "\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("자기 자신에게 친구 요청을 보낼 수 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 보내기 - 자기 자신에게 요청 (자기 자신의 초대코드로 친구 요청을 보낸 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .requestSchema(Schema.schema("CreateMateWebRequest"))
                        .requestFields(
                            fieldWithPath("invitationCode").type(STRING).description("상대방의 초대코드")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build()
                    )
                ));

            verify(mateService).requestMate(eq(1L), eq(invitationCode));
        }

        @Test
        void request_mate_fail_invalid_invitation_code_format_docs() throws Exception {
            // given - 잘못된 형식의 초대코드 (영문 대문자 6자리가 아닌 경우)

            // when & then
            mockMvc.perform(
                    post("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"abc\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("올바르지 않은 초대 코드 형식입니다 (영어 대문자 6자리)"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 보내기 - 잘못된 초대코드 형식 (초대코드가 영문 대문자 6자리 형식이 아닌 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .requestSchema(Schema.schema("CreateMateWebRequest"))
                        .requestFields(
                            fieldWithPath("invitationCode").type(STRING).description("잘못된 형식의 초대코드")
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
    @DisplayName("친구 전체 조회")
    class GetAcceptedMates {

        @Test
        void get_accepted_mates_docs() throws Exception {
            // given
            List<MateInfo> mateInfos = List.of(
                new MateInfo(1L, "토끼abc", "AAAAAA"),
                new MateInfo(2L, "고양이dd", "BBBBBB")
            );
            given(mateService.getAcceptedMates(eq(1L))).willReturn(mateInfos);

            // when & then
            mockMvc.perform(
                    get("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mateId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("토끼abc"))
                .andExpect(jsonPath("$[0].invitationCode").value("AAAAAA"))
                .andExpect(jsonPath("$[1].mateId").value(2))
                .andExpect(jsonPath("$[1].nickname").value("고양이dd"))
                .andExpect(jsonPath("$[1].invitationCode").value("BBBBBB"))
                .andDo(document("친구 전체 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .summary("친구 전체 조회")
                        .description("수락된(ACCEPTED) 친구 목록을 조회합니다.")
                        .responseSchema(Schema.schema("MateInfoWebResponse"))
                        .responseFields(
                            fieldWithPath("[].mateId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("친구 관계 ID"),
                            fieldWithPath("[].nickname").type(STRING)
                                .attributes(
                                    key("example").value("토끼abc")
                                )
                                .description("친구의 닉네임"),
                            fieldWithPath("[].invitationCode").type(STRING)
                                .attributes(
                                    key("example").value("AAAAAA"),
                                    key("format").value("영문 대문자 6자리")
                                )
                                .description("친구의 초대코드")
                        )
                        .build()
                    )
                ));

            verify(mateService).getAcceptedMates(eq(1L));
        }

        @Test
        void get_accepted_mates_empty_docs() throws Exception {
            // given
            given(mateService.getAcceptedMates(eq(1L))).willReturn(List.of());

            // when & then
            mockMvc.perform(
                    get("/mates")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andDo(document("친구 전체 조회 - 빈 목록 (수락된 친구가 없는 경우 빈 배열 반환)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .responseSchema(Schema.schema("MateInfoWebResponse"))
                        .build()
                    )
                ));

            verify(mateService).getAcceptedMates(eq(1L));
        }
    }

    @Nested
    @DisplayName("받은 친구 요청 조회")
    class GetReceivedRequests {

        @Test
        void get_received_requests_docs() throws Exception {
            // given
            List<MateReceivedInfo> receivedInfos = List.of(
                new MateReceivedInfo(3L, "강아지ee", "CCCCCC"),
                new MateReceivedInfo(4L, "다람쥐ff", "DDDDDD")
            );
            given(mateService.getReceivedRequests(eq(1L))).willReturn(receivedInfos);

            // when & then
            mockMvc.perform(
                    get("/mates/received")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mateId").value(3))
                .andExpect(jsonPath("$[0].nickname").value("강아지ee"))
                .andExpect(jsonPath("$[0].invitationCode").value("CCCCCC"))
                .andExpect(jsonPath("$[1].mateId").value(4))
                .andExpect(jsonPath("$[1].nickname").value("다람쥐ff"))
                .andExpect(jsonPath("$[1].invitationCode").value("DDDDDD"))
                .andDo(document("받은 친구 요청 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .summary("받은 친구 요청 조회")
                        .description("대기 중(PENDING)인 받은 친구 요청 목록을 조회합니다.")
                        .responseSchema(Schema.schema("MateReceivedWebResponse"))
                        .responseFields(
                            fieldWithPath("[].mateId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(3)
                                )
                                .description("친구 요청 ID"),
                            fieldWithPath("[].nickname").type(STRING)
                                .attributes(
                                    key("example").value("강아지ee")
                                )
                                .description("요청을 보낸 사용자의 닉네임"),
                            fieldWithPath("[].invitationCode").type(STRING)
                                .attributes(
                                    key("example").value("CCCCCC"),
                                    key("format").value("영문 대문자 6자리")
                                )
                                .description("요청을 보낸 사용자의 초대코드")
                        )
                        .build()
                    )
                ));

            verify(mateService).getReceivedRequests(eq(1L));
        }

        @Test
        void get_received_requests_empty_docs() throws Exception {
            // given
            given(mateService.getReceivedRequests(eq(1L))).willReturn(List.of());

            // when & then
            mockMvc.perform(
                    get("/mates/received")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andDo(document("받은 친구 요청 조회 - 빈 목록 (대기 중인 친구 요청이 없는 경우 빈 배열 반환)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .responseSchema(Schema.schema("MateReceivedWebResponse"))
                        .build()
                    )
                ));

            verify(mateService).getReceivedRequests(eq(1L));
        }
    }

    @Nested
    @DisplayName("친구 요청 수락/거절")
    class UpdateMateStatus {

        @Test
        void accept_mate_request_docs() throws Exception {
            // when & then
            mockMvc.perform(
                    patch("/mates/{mateId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}")
                )
                .andExpect(status().isOk())
                .andDo(document("친구 요청 수락",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .summary("친구 요청 수락")
                        .description("받은 친구 요청을 수락합니다. 수신자만 수락할 수 있습니다.")
                        .pathParameters(
                            parameterWithName("mateId").description("친구 요청 ID")
                        )
                        .requestSchema(Schema.schema("UpdateMateStatusWebRequest"))
                        .requestFields(
                            fieldWithPath("status").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(MateStatus.class)),
                                    key("example").value(MateStatus.ACCEPTED.name())
                                )
                                .description("변경할 상태. " + allowedValues(MateStatus.class))
                        )
                        .build()
                    )
                ));

            verify(mateService).updateMateStatus(eq(1L), eq(1L), eq(MateStatus.ACCEPTED));
        }

        @Test
        void reject_mate_request_docs() throws Exception {
            // when & then
            mockMvc.perform(
                    patch("/mates/{mateId}", 2L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}")
                )
                .andExpect(status().isOk())
                .andDo(document("친구 요청 거절",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .summary("친구 요청 거절")
                        .description("받은 친구 요청을 거절합니다. 수신자만 거절할 수 있습니다.")
                        .pathParameters(
                            parameterWithName("mateId").description("친구 요청 ID")
                        )
                        .requestSchema(Schema.schema("UpdateMateStatusWebRequest"))
                        .requestFields(
                            fieldWithPath("status").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(MateStatus.class)),
                                    key("example").value(MateStatus.REJECTED.name())
                                )
                                .description("변경할 상태. " + allowedValues(MateStatus.class))
                        )
                        .build()
                    )
                ));

            verify(mateService).updateMateStatus(eq(2L), eq(1L), eq(MateStatus.REJECTED));
        }

        @Test
        void update_mate_status_fail_not_found_docs() throws Exception {
            // given
            doThrow(new IllegalArgumentException("존재하지 않는 친구 요청입니다."))
                .when(mateService).updateMateStatus(eq(999L), eq(1L), eq(MateStatus.ACCEPTED));

            // when & then
            mockMvc.perform(
                    patch("/mates/{mateId}", 999L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 친구 요청입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 수락/거절 - 존재하지 않는 요청 (친구 요청 ID에 해당하는 요청이 존재하지 않는 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .pathParameters(
                            parameterWithName("mateId").description("친구 요청 ID")
                        )
                        .requestSchema(Schema.schema("UpdateMateStatusWebRequest"))
                        .requestFields(
                            fieldWithPath("status").type(STRING).description("변경할 상태")
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
        void update_mate_status_fail_not_receiver_docs() throws Exception {
            // given
            doThrow(new IllegalArgumentException("친구 요청의 수신자만 수락/거절할 수 있습니다."))
                .when(mateService).updateMateStatus(eq(1L), eq(1L), eq(MateStatus.ACCEPTED));

            // when & then
            mockMvc.perform(
                    patch("/mates/{mateId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("친구 요청의 수신자만 수락/거절할 수 있습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 수락/거절 - 권한 없음 (요청의 수신자가 아닌 사용자가 수락/거절을 시도한 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .pathParameters(
                            parameterWithName("mateId").description("친구 요청 ID")
                        )
                        .requestSchema(Schema.schema("UpdateMateStatusWebRequest"))
                        .requestFields(
                            fieldWithPath("status").type(STRING).description("변경할 상태")
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
        void update_mate_status_fail_invalid_status_docs() throws Exception {
            // when & then - 유효하지 않은 status 값
            mockMvc.perform(
                    patch("/mates/{mateId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("친구 요청 수락/거절 - 잘못된 status 값 (status 필드가 허용되지 않는 값인 경우)",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Mate")
                        .pathParameters(
                            parameterWithName("mateId").description("친구 요청 ID")
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
