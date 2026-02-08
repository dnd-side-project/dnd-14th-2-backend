package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerCategory;

public record CategoryAmountDto(
    LedgerCategory category,
    Long totalAmount
) {
}
