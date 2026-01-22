package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLedgerMemoWebRequest(
        @NotBlank
        @Size(max = 100)
        String memo
) {
    public UpdateLedgerMemoWebRequest {
        memo = memo.trim();
    }
}
