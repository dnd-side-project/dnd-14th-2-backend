package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.VerdictType;

public record VerdictJudgeWebRequest(
    VerdictType verdictType
) {
}
