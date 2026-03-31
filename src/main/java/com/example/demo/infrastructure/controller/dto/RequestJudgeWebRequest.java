package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;

public record RequestJudgeWebRequest(
    @NotNull
    Long ledgerEntryId
) {
}
