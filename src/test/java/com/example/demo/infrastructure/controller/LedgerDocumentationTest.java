package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.LedgerService;
import com.example.demo.application.dto.*;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.application.user.UserService;
import com.example.demo.common.config.ClockTestConfig;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.snippet.Attributes.key;

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

    @BeforeEach
    void setUpAuth() {
        final long userId = 1L;
        given(tokenProvider.validateAccessToken(accessToken)).willReturn(userId);
    }

    private static String enumNames(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", "));
    }

    private static String allowedValues(Class<? extends Enum<?>> enumType) {
        return "허용 값: [" + enumNames(enumType) + "]";
    }

    private static List<String> enumList(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toList());
    }

    private static String dateExample() {
        return "2026-01-24";
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
            .andDo(document("ledger-create",
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
                                key("example").value(dateExample())
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
                                key("example").value(dateExample())
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
            .andDo(document("ledger-get",
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
                                key("example").value(dateExample())
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
            .andDo(document("ledger-memo-update",
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
            .andDo(document("ledger-update",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Ledger")
                    .summary("가계부 항목 수정")
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
                                key("example").value(dateExample())
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
                                key("example").value(dateExample())
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
    void delete_ledger_entry_docs() throws Exception {
        willDoNothing().given(ledgerService).deleteLedgerEntry(eq(1L), eq(1L));

        mockMvc.perform(
                delete("/ledgers/{ledgerId}", 1L)
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andDo(document("ledger-delete",
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
            .andDo(document("ledger-summary",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Ledger")
                    .summary("가계부 요약 조회")
                    .queryParameters(
                        parameterWithName("start").optional().description("조회 시작일(yyyy-MM-dd), 미입력 시 기본값 적용"),
                        parameterWithName("end").optional().description("조회 종료일(yyyy-MM-dd), 미입력 시 기본값 적용")
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
                                key("example").value(dateExample())
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

}
