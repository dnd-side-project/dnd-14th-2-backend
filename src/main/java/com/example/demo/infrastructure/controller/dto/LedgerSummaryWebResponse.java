package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerResult;

import java.time.LocalDate;
import java.util.List;

public record LedgerSummaryWebResponse(
    LocalDate start,
    LocalDate end,
    List<LedgerDetailWebResponse> result
) {
    public static LedgerSummaryWebResponse from(
        LocalDate start,
        LocalDate end,
        List<LedgerResult> rawResult
    ) {
        List<LedgerDetailWebResponse> result = rawResult.stream()
            .map(LedgerDetailWebResponse::from)
            .toList();

        return new LedgerSummaryWebResponse(start, end, result);
    }
}
