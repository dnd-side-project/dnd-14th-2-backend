package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.VerdictType;
import jakarta.validation.constraints.NotNull;

public record VerdictJudgeWebRequest(
    @NotNull(message = "심판 유형(type)은 필수입니다.")
    VerdictType verdictType
) {
}
