package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.User;
import com.example.demo.domain.Verdict;
import com.example.demo.domain.VerdictType;

public record JurorVerdict(
    Long id,
    UserInfo defendantInfo,
    LedgerEntryInfo ledgerEntryInfo,
    VerdictType verdictType
) {
    public static JurorVerdict from(Verdict verdict) {
        LedgerEntry ledgerEntry = verdict.getLedgerEntry();
        User defendant = verdict.getLedgerEntry().getUser();

        return new JurorVerdict(
            verdict.getId(),
            UserInfo.from(defendant),
            LedgerEntryInfo.from(ledgerEntry),
            verdict.getType()
        );
    }
}
