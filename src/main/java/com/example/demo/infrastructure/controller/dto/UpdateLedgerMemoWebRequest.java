package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLedgerMemoWebRequest(
    @NotBlank(message = "메모(memo)는 공백일 수 없습니다.")
    @Size(max = 100, message = "메모(memo)는 최대 100자까지 입력할 수 있습니다.")
    String memo
) {
    public UpdateLedgerMemoWebRequest {
        memo = memo.trim();
    }
}
