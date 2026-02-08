package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import java.time.LocalDate;
import java.util.Map;

public record LedgerStatisticsWebResponse(
    LedgerType type,
    Map<LedgerCategory, Long> categoryAmounts,
    Long currentMonthTotalAmount,
    Long lastMonthTotalAmount,
    LocalDate startDate,
    LocalDate endDate
) {
    public static LedgerStatisticsWebResponse from(LedgerStatisticsResponse response) {
        return new LedgerStatisticsWebResponse(
            response.type(),
            response.categoryAmounts(),
            response.currentMonthTotalAmount(),
            response.lastMonthTotalAmount(),
            response.startDate(),
            response.endDate()
        );
    }
}