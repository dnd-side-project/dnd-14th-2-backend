package com.example.demo.application.dto;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;

import java.time.LocalDate;

public record UpsertLedgerCommand(
    long userId,
    long amount,
    LedgerType type,
    LedgerCategory category,
    String description,
    LocalDate occurredOn,
    PaymentMethod paymentMethod,
    String memo
) {
}
