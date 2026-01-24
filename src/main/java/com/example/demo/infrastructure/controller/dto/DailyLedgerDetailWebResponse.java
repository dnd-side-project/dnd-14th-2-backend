package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.DailyLedgerDetail;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

public record DailyLedgerDetailWebResponse(
    LocalDate date,
    long incomeTotal,
    long expenseTotal,
    List<DailyLedgerEntryWebResponse> entries
) {
    public DailyLedgerDetailWebResponse(DailyLedgerDetail detail) {
        this(
            detail.date(),
            detail.incomeTotal(),
            detail.expenseTotal(),
            detail.results().stream()
                .map(DailyLedgerEntryWebResponse::from)
                .toList()
        );
    }

    public record DailyLedgerEntryWebResponse(
        Long ledgerId,
        Long amount,
        LedgerType type,
        LedgerCategory category,
        String description,
        PaymentMethod paymentMethod
    ) {
        public static DailyLedgerEntryWebResponse from(LedgerResult ledgerResult) {
            return new DailyLedgerEntryWebResponse(
                ledgerResult.ledgerId(),
                ledgerResult.amount(),
                ledgerResult.type(),
                ledgerResult.category(),
                ledgerResult.description(),
                ledgerResult.paymentMethod()

            );
        }
    }
}