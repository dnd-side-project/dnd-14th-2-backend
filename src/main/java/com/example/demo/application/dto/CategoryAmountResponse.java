package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerCategory;

public record CategoryAmountResponse(
    LedgerCategory category,
    Long totalAmount
) {
}
