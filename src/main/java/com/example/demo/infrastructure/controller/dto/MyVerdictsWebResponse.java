package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.MyVerdicts;
import java.util.List;

public record MyVerdictsWebResponse(
    List<MyVerdictWebResponse> verdicts
) {

    public static MyVerdictsWebResponse from(MyVerdicts myVerdicts) {
        return new MyVerdictsWebResponse(myVerdicts.verdicts().stream()
            .map(MyVerdictWebResponse::from)
            .toList());
    }
}
