package com.example.demo.application.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyLedgerDetail(
    LocalDate date,
    long incomeTotal,
    long expenseTotal,
    List<LedgerResult> results
) {
}