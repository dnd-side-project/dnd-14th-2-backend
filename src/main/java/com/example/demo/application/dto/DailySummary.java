package com.example.demo.application.dto;

import java.time.LocalDate;

public record DailySummary(
    LocalDate date,
    long incomeTotal,
    long expenseTotal
) {
}
