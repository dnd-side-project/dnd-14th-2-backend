package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerCategory;

public record LedgerEntryInfo(
    Long id,
    Long amount,
    LedgerCategory category,
    String description
) {
}
