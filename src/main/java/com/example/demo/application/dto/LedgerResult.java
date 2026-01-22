package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;

import java.time.LocalDate;

public record LedgerResult(
        Long ledgerId,
        Long amount,
        LedgerType type,
        LedgerCategory category,
        String description,
        LocalDate occurredOn,
        PaymentMethod paymentMethod
) {
    public static LedgerResult from(LedgerEntry entry) {
        return new LedgerResult(
                entry.getId(),
                entry.getAmount(),
                entry.getType(),
                entry.getCategory(),
                entry.getDescription(),
                entry.getOccurredOn(),
                entry.getPaymentMethod()
        );
    }
}
