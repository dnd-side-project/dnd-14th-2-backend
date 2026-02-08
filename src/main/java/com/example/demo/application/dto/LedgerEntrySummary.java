package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.enums.LedgerCategory;

public record LedgerEntrySummary(
    LedgerCategory category,
    Long amount
) {
    public static LedgerEntrySummary from(LedgerEntry entry) {
        return new LedgerEntrySummary(
            entry.getCategory(),
            entry.getAmount()
        );
    }
}
