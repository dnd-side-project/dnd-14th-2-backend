package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.JurorVerdict;
import com.example.demo.application.dto.LedgerEntryInfo;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.VerdictType;

public record JurorVerdictWebResponse(
    Long id,
    UserInfoWebResponse defendantInfo,
    LedgerEntryInfoWebResponse ledgerEntryInfo,
    VerdictType verdictType
) {
    public static JurorVerdictWebResponse from(JurorVerdict verdict) {
        LedgerEntryInfo entry = verdict.ledgerEntryInfo();
        UserInfo defendant = verdict.defendantInfo();

        return new JurorVerdictWebResponse(
            verdict.id(),
            UserInfoWebResponse.from(defendant),
            new LedgerEntryInfoWebResponse(
                entry.id(),
                entry.amount(),
                entry.category(),
                entry.paymentMethod(),
                entry.description()
            ),
            verdict.verdictType()
        );
    }
}
