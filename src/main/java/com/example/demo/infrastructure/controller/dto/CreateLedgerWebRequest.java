package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.CreateLedgerCommand;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLedgerWebRequest(
        @NotNull
        @Positive
        Long amount,                    // 9,223,372,036,854,775,807원

        @NotNull
        LedgerType type,

        @NotNull
        LedgerCategory category,

        @NotBlank
        @Size(max = 15)
        String description,

        @NotNull
        LocalDate occurredOn,

        @NotNull
        PaymentMethod paymentMethod
) {

    public CreateLedgerCommand toCommand(Long userId) {
        return new CreateLedgerCommand(
                userId,
                amount,
                type,
                category,
                description,
                occurredOn,
                paymentMethod
        );
    }
}
