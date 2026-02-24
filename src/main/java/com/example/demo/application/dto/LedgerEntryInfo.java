package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.PaymentMethod;

public record LedgerEntryInfo(
    Long id,
    Long amount,
    LedgerCategory category,
    PaymentMethod paymentMethod,
    String description
) {
    public static LedgerEntryInfo from(LedgerEntry ledgerEntry) {
        return new LedgerEntryInfo(
            ledgerEntry.getId(),
            ledgerEntry.getAmount(),
            ledgerEntry.getCategory(),
            ledgerEntry.getPaymentMethod(),
            ledgerEntry.getDescription()
        );
    }
}
