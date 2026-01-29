package com.example.demo.infrastructure.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.demo.application.LedgerService;
import com.example.demo.application.UserService;
import com.example.demo.application.dto.*;
import com.example.demo.application.oauth.TokenProvider;
import com.example.demo.common.config.ClockTestConfig;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
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

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerController.class)
@AutoConfigureRestDocs
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
        given(tokenProvider.validateToken(accessToken)).willReturn(userId);
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
                        fieldWithPath("amount").type(NUMBER).description("금액"),
                        fieldWithPath("type").type(STRING).description("유형(INCOME/EXPENSE)"),
                        fieldWithPath("category").type(STRING).description("카테고리"),
                        fieldWithPath("description").type(STRING).description("설명"),
                        fieldWithPath("occurredOn").type(STRING).description("발생 일자(yyyy-MM-dd)"),
                        fieldWithPath("paymentMethod").type(STRING).description("결제 수단"),
                        fieldWithPath("memo").type(STRING).optional().description("메모(선택)")
                    )
                    .responseFields(
                        fieldWithPath("ledgerId").type(NUMBER).description("생성된 가계부 항목 ID"),
                        fieldWithPath("amount").type(NUMBER).description("금액"),
                        fieldWithPath("type").type(STRING).description("유형(INCOME/EXPENSE)"),
                        fieldWithPath("category").type(STRING).description("카테고리"),
                        fieldWithPath("description").type(STRING).description("설명"),
                        fieldWithPath("occurredOn").type(STRING).description("발생 일자(yyyy-MM-dd)"),
                        fieldWithPath("paymentMethod").type(STRING).description("결제 수단"),
                        fieldWithPath("memo").type(STRING).optional().description("메모(선택)")
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
                        fieldWithPath("ledgerId").type(NUMBER).description("가계부 항목 ID"),
                        fieldWithPath("amount").type(NUMBER).description("금액"),
                        fieldWithPath("type").type(STRING).description("유형(INCOME/EXPENSE)"),
                        fieldWithPath("category").type(STRING).description("카테고리"),
                        fieldWithPath("description").type(STRING).description("설명"),
                        fieldWithPath("occurredOn").type(STRING).optional().description("발생 일자(yyyy-MM-dd)"),
                        fieldWithPath("paymentMethod").type(STRING).description("결제 수단"),
                        fieldWithPath("memo").type(STRING).optional().description("메모(선택)")
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
                        fieldWithPath("memo").type(STRING).description("메모")
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
                        fieldWithPath("amount").type(NUMBER).description("금액"),
                        fieldWithPath("type").type(STRING).description("유형(INCOME/EXPENSE)"),
                        fieldWithPath("category").type(STRING).description("카테고리"),
                        fieldWithPath("description").type(STRING).description("설명"),
                        fieldWithPath("occurredOn").type(STRING).description("발생 일자(yyyy-MM-dd)"),
                        fieldWithPath("paymentMethod").type(STRING).description("결제 수단"),
                        fieldWithPath("memo").type(STRING).optional().description("메모(선택)")
                    )
                    .responseFields(
                        fieldWithPath("ledgerId").type(NUMBER).description("가계부 항목 ID"),
                        fieldWithPath("amount").type(NUMBER).description("금액"),
                        fieldWithPath("type").type(STRING).description("유형(INCOME/EXPENSE)"),
                        fieldWithPath("category").type(STRING).description("카테고리"),
                        fieldWithPath("description").type(STRING).description("설명"),
                        fieldWithPath("occurredOn").type(STRING).optional().description("발생 일자(yyyy-MM-dd)"),
                        fieldWithPath("paymentMethod").type(STRING).description("결제 수단"),
                        fieldWithPath("memo").type(STRING).optional().description("메모(선택)")
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

        List<LedgerResult> summaries = List.of(Mockito.mock(LedgerResult.class));
        given(ledgerService.getSummary(eq(1L), eq(start), eq(end))).willReturn(summaries);

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
                    .build())
            ));
    }

}