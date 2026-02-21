package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.enums.LedgerCategory;

public record LedgerEntryInfoWebResponse(
    Long id,
    Long amount,
    LedgerCategory category,
    String description
) {
}
