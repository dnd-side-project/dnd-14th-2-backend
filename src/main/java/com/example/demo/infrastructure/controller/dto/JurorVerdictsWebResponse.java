package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.JurorVerdicts;
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
