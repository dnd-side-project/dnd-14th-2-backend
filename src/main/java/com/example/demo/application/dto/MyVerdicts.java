package com.example.demo.application.dto;

import com.example.demo.domain.Verdict;
import java.util.List;

public record MyVerdicts(
    List<MyVerdict> verdicts
) {

    public static MyVerdicts from(List<Verdict> verdicts) {
        return new MyVerdicts(verdicts.stream()
            .map(MyVerdict::from)
            .toList());
    }
}
