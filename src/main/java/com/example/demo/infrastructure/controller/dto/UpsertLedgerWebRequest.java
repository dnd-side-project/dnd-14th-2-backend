package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpsertLedgerWebRequest(
    @NotNull(message = "금액(amount)은 필수입니다.")
    @Positive(message = "금액(amount)은 0보다 커야 합니다.")
    Long amount,

    @NotNull(message = "거래 유형(type)은 필수입니다.")
    LedgerType type,

    @NotNull(message = "카테고리(category)는 필수입니다.")
    LedgerCategory category,

    @NotBlank(message = "설명(description)은 공백일 수 없습니다.")
    @Size(max = 15, message = "설명(description)은 최대 15자까지 입력할 수 있습니다.")
    String description,

    @NotNull(message = "가계부 등록 일자(occurredOn)는 필수입니다.")
    LocalDate occurredOn,

    @NotNull(message = "결제 수단(paymentMethod)은 필수입니다.")
    PaymentMethod paymentMethod,

    @NotBlank(message = "메모(memo)는 공백일 수 없습니다.")
    @Size(max = 100, message = "메모(memo)는 최대 100자까지 입력할 수 있습니다.")
    String memo
) {

    public UpsertLedgerCommand toCommand(Long userId) {
        return new UpsertLedgerCommand(
            userId,
            amount,
            type,
            category,
            description,
            occurredOn,
            paymentMethod,
            memo
        );
    }
}
