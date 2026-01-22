package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerResult;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;

import java.time.LocalDate;

public record LedgerDetailWebResponse(
        Long ledgerId,
        Long amount,
        LedgerType type,
        LedgerCategory category,
        String description,
        LocalDate occurredOn,
        PaymentMethod paymentMethod
) {
    public static LedgerDetailWebResponse from(LedgerResult ledgerResult) {
        return new LedgerDetailWebResponse(
                ledgerResult.ledgerId(),
                ledgerResult.amount(),
                ledgerResult.type(),
                ledgerResult.category(),
                ledgerResult.description(),
                ledgerResult.occurredOn(),
                ledgerResult.paymentMethod()
        );
    }
}
