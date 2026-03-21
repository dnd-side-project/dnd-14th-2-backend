package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestJudgeWebRequest(
    @NotBlank
    Long ledgerEntryId
) {
}
