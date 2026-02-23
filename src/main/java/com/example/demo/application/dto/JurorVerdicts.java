package com.example.demo.application.dto;

import com.example.demo.domain.Verdict;
import java.util.List;

public record JurorVerdicts(
    List<JurorVerdict> jurorVerdicts
) {
    public static JurorVerdicts from(List<Verdict> verdicts) {
        return new JurorVerdicts(verdicts.stream()
            .map(JurorVerdict::from)
            .toList());
    }
}
