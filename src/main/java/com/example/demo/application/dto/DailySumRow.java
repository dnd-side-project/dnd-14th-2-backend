package com.example.demo.application.dto;

import java.time.LocalDate;

public record DailySumRow(
    LocalDate date,
    long incomeTotal,
    long expenseTotal
) {
}