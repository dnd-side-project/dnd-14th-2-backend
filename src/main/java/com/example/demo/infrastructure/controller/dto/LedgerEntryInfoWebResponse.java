package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.PaymentMethod;

public record LedgerEntryInfoWebResponse(
    Long id,
    Long amount,
    LedgerCategory category,
    PaymentMethod paymentMethod,
    String description
) {
}
