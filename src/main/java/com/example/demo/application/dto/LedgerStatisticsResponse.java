package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import java.time.LocalDate;
import java.util.Map;

public record LedgerStatisticsResponse(
    LedgerType type,
    Map<LedgerCategory, Long> categoryAmounts,
    Long currentMonthTotalAmount,
    Long lastMonthTotalAmount,
    LocalDate startDate,
    LocalDate endDate
) {
}
