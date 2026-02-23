package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.JurorVerdict;
import com.example.demo.application.dto.JurorVerdicts;
import com.example.demo.domain.Verdict;
import java.util.List;

public record JurorVerdictsWebResponse(
    List<JurorVerdictWebResponse> jurorVerdicts
) {
    public static JurorVerdictsWebResponse from(JurorVerdicts verdicts) {
        return new JurorVerdictsWebResponse(verdicts.jurorVerdicts().stream()
            .map(JurorVerdictWebResponse::from)
            .toList());
    }
}
