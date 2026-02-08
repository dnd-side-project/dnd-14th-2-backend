package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.enums.LedgerType;
import java.util.Map;

public record LedgerStatisticsWebResponse(
    LedgerType type,
    Map<String, Long> categoryAmounts,
    Long currentMonthTotalAmount,
    Long lastMonthTotalAmount
) {
    public static LedgerStatisticsWebResponse from(LedgerStatisticsResponse response) {
        return new LedgerStatisticsWebResponse(
            response.type(),
            response.categoryAmounts(),
            response.currentMonthTotalAmount(),
            response.lastMonthTotalAmount()
        );
    }
}