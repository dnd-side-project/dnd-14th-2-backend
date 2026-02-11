package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.LedgerService;
import com.example.demo.application.dto.DateRange;
import com.example.demo.application.dto.LedgerEntriesByDateRangeResponse;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.application.user.UserService;
import com.example.demo.common.config.ClockTestConfig;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.demo.util.RestDocsUtils.allowedValues;
import static com.example.demo.util.RestDocsUtils.enumList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LedgerController.class)
@AutoConfigureRestDocs
@Tag("restdocs")
@Import(ClockTestConfig.class)
class LedgerDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerService ledgerService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private TokenProvider tokenProvider;


    private final String accessToken = "jwt.access.token";

    @Nested
    @DisplayName("가계부 생성")
    class CreateLedgerEntry {
        @Test
        void create_ledger_entry_docs() throws Exception {
            given(ledgerService.createLedgerEntry(any(UpsertLedgerCommand.class)))
                .willReturn(sampleResult(1L));

            mockMvc.perform(
                    post("/ledgers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 12000,
                              "type": "EXPENSE",
                              "category": "FOOD",
                              "description": "점심",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "메모"
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ledgerId").value(1))
                .andDo(document("가계부 생성",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 생성")
                        .requestSchema(Schema.schema("UpsertLedgerWebRequest"))
                        .responseSchema(Schema.schema("LedgerDetailWebResponse"))
                        .requestFields(
                            fieldWithPath("amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자 (yyyy-MM-dd)"),
                            fieldWithPath("paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .responseFields(
                            fieldWithPath("ledgerId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("생성된 가계부 항목 ID"),
                            fieldWithPath("amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자(yyyy-MM-dd)"),
                            fieldWithPath("paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .build())
                ));
        }

        @Test
        void create_ledger_entry_fail_not_exists_user_docs() throws Exception {
            // given
            given(ledgerService.createLedgerEntry(any(UpsertLedgerCommand.class)))
                .willThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."));

            // when & then
            mockMvc.perform(
                    post("/ledgers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 12000,
                              "type": "EXPENSE",
                              "category": "FOOD",
                              "description": "점심",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "메모"
                            }
                            """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 생성 - 존재하지 않는 사용자",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 생성 - 존재하지 않는 사용자")
                        .description("가계부 항목 생성 요청 시, 사용자 정보가 존재하지 않아 생성에 실패한 경우")
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }


        @Test
        void create_ledger_entry_fail_invalid_enum_docs() throws Exception {
            // when & then
            mockMvc.perform(
                    post("/ledgers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 12000,
                              "type": "INVALID",
                              "category": "FOOD",
                              "description": "점심",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "메모"
                            }
                            """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 생성 - 잘못된 enum 값",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 생성 - 요청 값 오류")
                        .description("가계부 항목 생성 요청에서 enum 필드(type/category/paymentMethod)가 허용되지 않는 값인 경우")
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }

    @Nested
    @DisplayName("가계부 단건 조회")
    class GetLedgerEntryById {
        @Test
        void get_ledger_entry_by_id_docs() throws Exception {
            given(ledgerService.getLedgerEntry(eq(1L), eq(1L)))
                .willReturn(sampleResult(1L));

            mockMvc.perform(
                    get("/ledgers/{ledgerId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ledgerId").value(1))
                .andDo(document("가계부 단건 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 단건 조회")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("LedgerDetailWebResponse"))
                        .responseFields(
                            fieldWithPath("ledgerId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("가계부 항목 ID"),
                            fieldWithPath("amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자(yyyy-MM-dd)"),
                            fieldWithPath("paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .build())
                ));
        }

        @Test
        void get_ledger_entry_by_id_fail_not_found_docs() throws Exception {
            // given
            given(ledgerService.getLedgerEntry(eq(1L), eq(999L)))
                .willThrow(new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));

            // when & then
            mockMvc.perform(
                    get("/ledgers/{ledgerId}", 999L)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("해당되는 가계부 항목이 존재하지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 단건 조회 - 항목 없음",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 단건 조회 - 항목 없음")
                        .description("조회하려는 가계부 항목이 존재하지 않거나, 해당 사용자의 항목이 아닌 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }


    @Nested
    @DisplayName("가계부 메모 수정")
    class UpdateLedgerMemo {
        @Test
        void update_ledger_memo_docs() throws Exception {
            willDoNothing().given(ledgerService).updateLedgerMemo(eq(1L), eq(1L), eq("새 메모"));

            mockMvc.perform(
                    patch("/ledgers/{ledgerId}/memo", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"memo\":\"새 메모\"}")
                )
                .andExpect(status().isNoContent())
                .andDo(document("가계부 메모 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 메모 수정")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .requestSchema(Schema.schema("UpdateLedgerMemoWebRequest"))
                        .requestFields(
                            fieldWithPath("memo").type(STRING)
                                .attributes(key("example").value("새 메모"))
                                .description("메모")
                        )
                        .build())
                ));
        }

        @Test
        void update_ledger_memo_fail_not_found_docs() throws Exception {
            // given
            Mockito.doThrow(new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."))
                .when(ledgerService).updateLedgerMemo(eq(1L), eq(999L), any(String.class));

            // when & then
            mockMvc.perform(
                    patch("/ledgers/{ledgerId}/memo", 999L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"memo\":\"새 메모\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("해당되는 가계부 항목이 존재하지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 메모 수정 - 항목 없음",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 메모 수정 - 항목 없음")
                        .description("수정하려는 가계부 항목이 존재하지 않거나, 해당 사용자의 항목이 아닌 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }

        @Test
        void update_ledger_memo_fail_exceed_max_length_docs() throws Exception {
            // given - 101자 메모 생성
            String longMemo = "a".repeat(101);

            // when & then
            mockMvc.perform(
                    patch("/ledgers/{ledgerId}/memo", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"memo\":\"" + longMemo + "\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("메모(memo)는 최대 100자까지 입력할 수 있습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 메모 수정 - 길이 초과",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 메모 수정 - 길이 초과")
                        .description("메모가 최대 길이(100자)를 초과한 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .requestSchema(Schema.schema("UpdateLedgerMemoWebRequest"))
                        .requestFields(
                            fieldWithPath("memo").type(STRING)
                                .description("메모 (101자 이상)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }

    @Nested
    @DisplayName("가계부 항목 전체 수정")
    class UpdateLedgerEntry {
        @Test
        void update_ledger_entry_docs() throws Exception {
            given(ledgerService.updateLedgerEntry(eq(1L), any(UpsertLedgerCommand.class)))
                .willReturn(sampleResult(1L));

            mockMvc.perform(
                    put("/ledgers/{ledgerId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 15000,
                              "type": "EXPENSE",
                              "category": "FOOD",
                              "description": "저녁",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "수정된 메모"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ledgerId").value(1))
                .andDo(document("가계부 항목 전체 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 전체 수정")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .requestSchema(Schema.schema("UpsertLedgerWebRequest"))
                        .responseSchema(Schema.schema("LedgerDetailWebResponse"))
                        .requestFields(
                            fieldWithPath("amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자(yyyy-MM-dd)"),
                            fieldWithPath("paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .responseFields(
                            fieldWithPath("ledgerId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("가계부 항목 ID"),
                            fieldWithPath("amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자(yyyy-MM-dd)"),
                            fieldWithPath("paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .build())
                ));
        }

        @Test
        void update_ledger_entry_fail_not_found_docs() throws Exception {
            // given
            given(ledgerService.updateLedgerEntry(eq(999L), any(UpsertLedgerCommand.class)))
                .willThrow(new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));

            // when & then
            mockMvc.perform(
                    put("/ledgers/{ledgerId}", 999L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 15000,
                              "type": "EXPENSE",
                              "category": "FOOD",
                              "description": "저녁",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "수정된 메모"
                            }
                            """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("해당되는 가계부 항목이 존재하지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 항목 전체 수정 - 항목 없음",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 전체 수정 - 항목 없음")
                        .description("수정하려는 가계부 항목이 존재하지 않거나, 해당 사용자의 항목이 아닌 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }

        @Test
        void update_ledger_entry_fail_invalid_request_docs() throws Exception {
            // when & then
            mockMvc.perform(
                    put("/ledgers/{ledgerId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 15000,
                              "type": "INVALID_TYPE",
                              "category": "FOOD",
                              "description": "저녁",
                              "occurredOn": "2026-01-24",
                              "paymentMethod": "CREDIT_CARD",
                              "memo": "수정된 메모"
                            }
                            """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 항목 전체 수정 - 요청 값 오류",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 전체 수정 - 요청 값 오류")
                        .description("가계부 항목 수정 요청에서 enum 필드가 허용되지 않는 값인 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }

    @Nested
    @DisplayName("가계부 항목 삭제")
    class DeleteLedgerEntry {
        @Test
        void delete_ledger_entry_docs() throws Exception {
            willDoNothing().given(ledgerService).deleteLedgerEntry(eq(1L), eq(1L));

            mockMvc.perform(
                    delete("/ledgers/{ledgerId}", 1L)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andDo(document("가계부 항목 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 삭제")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .build())
                ));
        }

        @Test
        void delete_ledger_entry_fail_not_found_docs() throws Exception {
            // given
            Mockito.doThrow(new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."))
                .when(ledgerService).deleteLedgerEntry(eq(1L), eq(999L));

            // when & then
            mockMvc.perform(
                    delete("/ledgers/{ledgerId}", 999L)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("해당되는 가계부 항목이 존재하지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 항목 삭제 - 항목 없음",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 항목 삭제 - 항목 없음")
                        .description("삭제하려는 가계부 항목이 존재하지 않거나, 해당 사용자의 항목이 아닌 경우")
                        .pathParameters(
                            parameterWithName("ledgerId").description("가계부 항목 ID")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }

    @Nested
    @DisplayName("가계부 요약 조회")
    class GetLedgerSummary {
        @Test
        void get_ledger_summary_docs() throws Exception {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 1, 31);

            UserInfo userInfo = Mockito.mock(UserInfo.class);
            given(userService.getUserInfo(eq(1L))).willReturn(userInfo);

            LedgerEntriesByDateRangeResponse response = new LedgerEntriesByDateRangeResponse(
                new DateRange(start, end),
                List.of(
                    sampleResult(1L),
                    sampleResult(2L),
                    sampleResult(3L)
                )
            );
            given(ledgerService.getSummary(eq(1L), eq(start), eq(end))).willReturn(response);

            mockMvc.perform(
                    get("/ledgers/summary")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andDo(document("가계부 요약 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 요약 조회")
                        .queryParameters(
                            parameterWithName("start").description("조회 시작일(yyyy-MM-dd), 미입력 시 기본값 적용"),
                            parameterWithName("end").description("조회 종료일(yyyy-MM-dd), 미입력 시 기본값 적용")
                        )
                        .responseSchema(Schema.schema("LedgerSummaryWebResponse"))
                        .responseFields(
                            fieldWithPath("start").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-01")
                                )
                                .description("조회 시작일(yyyy-MM-dd)"),
                            fieldWithPath("end").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-31")
                                )
                                .description("조회 종료일(yyyy-MM-dd)"),
                            fieldWithPath("result").type(ARRAY)
                                .description("일자 범위 내 가계부 항목 목록"),

                            fieldWithPath("result[].ledgerId").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(1)
                                )
                                .description("가계부 항목 ID"),
                            fieldWithPath("result[].amount").type(NUMBER)
                                .attributes(
                                    key("format").value("int64"),
                                    key("example").value(12000)
                                )
                                .description("금액 (1원 이상 ~ 9,223,372,036,854,775,807원 이하 금액만 가능)"),
                            fieldWithPath("result[].type").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerType.class)),
                                    key("example").value(LedgerType.EXPENSE.name())
                                )
                                .description("유형. " + allowedValues(LedgerType.class)),
                            fieldWithPath("result[].category").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(LedgerCategory.class)),
                                    key("example").value(LedgerCategory.FOOD.name())
                                )
                                .description("카테고리. " + allowedValues(LedgerCategory.class)),
                            fieldWithPath("result[].description").type(STRING)
                                .attributes(
                                    key("example").value("점심")
                                )
                                .description("가계부 내용"),
                            fieldWithPath("result[].occurredOn").type(STRING)
                                .attributes(
                                    key("format").value("date"),
                                    key("example").value("2026-01-24")
                                )
                                .description("소비/지출 일자(yyyy-MM-dd)"),
                            fieldWithPath("result[].paymentMethod").type(STRING)
                                .attributes(
                                    key("enum").value(enumList(PaymentMethod.class)),
                                    key("example").value(PaymentMethod.CREDIT_CARD.name())
                                )
                                .description("결제 수단. " + allowedValues(PaymentMethod.class)),
                            fieldWithPath("result[].memo").type(STRING).optional()
                                .attributes(
                                    key("example").value("메모")
                                )
                                .description("메모(선택)")
                        )
                        .build())
                ));
        }

        @Test
        void get_ledger_summary_fail_invalid_date_format_docs() throws Exception {
            // when & then
            mockMvc.perform(
                    get("/ledgers/summary")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("start", "2026-13-01")
                        .param("end", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(containsString("요청 파라미터 형식이 올바르지 않습니다")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 요약 조회 - 날짜 형식 오류",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 요약 조회 - 날짜 형식 오류")
                        .description("날짜 파라미터(start, end)가 yyyy-MM-dd 형식이 아니거나 유효하지 않은 날짜인 경우")
                        .queryParameters(
                            parameterWithName("start").description("조회 시작일(yyyy-MM-dd)"),
                            parameterWithName("end").description("조회 종료일(yyyy-MM-dd)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("요청 파라미터 형식이 올바르지 않습니다 : start"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }

        @Test
        void get_ledger_summary_fail_missing_date_param_docs() throws Exception {
            // when & then - start 파라미터 누락
            mockMvc.perform(
                    get("/ledgers/summary")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("end", "2026-01-31")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(containsString("필수 요청 파라미터가 누락되었습니다"))).andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("가계부 요약 조회 - 날짜 파라미터 누락",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                        .tag("Ledger")
                        .summary("가계부 요약 조회 - 날짜 파라미터 누락")
                        .description("필수 쿼리 파라미터(start 또는 end)가 누락된 경우")
                        .queryParameters(
                            parameterWithName("end").description("조회 종료일(yyyy-MM-dd)")
                        )
                        .responseSchema(Schema.schema("ErrorResponse"))
                        .responseFields(
                            fieldWithPath("message").type(STRING).description("에러 메시지"),
                            fieldWithPath("timestamp").type(STRING).description("예외 발생 시각")
                        )
                        .build())
                ));
        }
    }

    @BeforeEach
    void setUpAuth() {
        final long userId = 1L;
        given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
    }


    private LedgerResult sampleResult(Long ledgerId) {
        return new LedgerResult(
            ledgerId,
            12000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "점심",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "메모"
        );
    }
}
