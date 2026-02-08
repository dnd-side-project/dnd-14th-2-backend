package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.enums.LedgerType;
import java.util.Map;

public record LedgerStatisticsWebResponse(
    LedgerType type,
    Map<String, Long> categoryAmounts,
    Long currentMonthTotal,
    Long lastMonthTotal
) {
    public static LedgerStatisticsWebResponse from(LedgerStatisticsResponse response) {
        return new LedgerStatisticsWebResponse(
            response.type(),
            response.categoryAmounts(),
            response.currentMonthTotal(),
            response.lastMonthTotal()
        );
    }
}