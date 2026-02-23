package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.enums.LedgerCategory;

public record LedgerEntryInfo(
    Long id,
    Long amount,
    LedgerCategory category,
    String description
) {
    public static LedgerEntryInfo from(LedgerEntry ledgerEntry) {
        return new LedgerEntryInfo(
            ledgerEntry.getId(),
            ledgerEntry.getAmount(),
            ledgerEntry.getCategory(),
            ledgerEntry.getDescription()
        );
    }
}
