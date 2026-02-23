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
            new UserInfo(
                defendant.getId(),
                defendant.getNickname(),
                defendant.getLevel(),
                defendant.getProfile()
            ),
            new LedgerEntryInfo(
                ledgerEntry.getId(),
                ledgerEntry.getAmount(),
                ledgerEntry.getCategory(),
                ledgerEntry.getDescription()
            ),
            verdict.getType()
        );
    }
}
