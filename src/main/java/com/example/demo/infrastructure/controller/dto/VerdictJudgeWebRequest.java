package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.VerdictType;

public record VerdictJudgeWebRequest(
    Long verdictId,
    VerdictType verdictType
) {
}
