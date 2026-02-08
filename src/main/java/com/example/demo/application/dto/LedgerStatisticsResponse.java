package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerType;
import java.util.Map;

public record LedgerStatisticsResponse (
    LedgerType type,
    Map<String, Long> categoryAmounts,
    Long currentMonthTotalAmount,
    Long lastMonthTotalAmount
){
}
