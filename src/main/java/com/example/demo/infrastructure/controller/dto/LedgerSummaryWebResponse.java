package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerEntriesByDateRangeResponse;
import java.time.LocalDate;
import java.util.List;

public record LedgerSummaryWebResponse(
    LocalDate start,
    LocalDate end,
    List<LedgerDetailWebResponse> result
) {
    public static LedgerSummaryWebResponse from(
        LedgerEntriesByDateRangeResponse response
    ) {
        List<LedgerDetailWebResponse> result = response.results().stream()
            .map(LedgerDetailWebResponse::from)
            .toList();

        return new LedgerSummaryWebResponse(
            response.dateRange().start(),
            response.dateRange().end(),
            result
        );
    }
}
