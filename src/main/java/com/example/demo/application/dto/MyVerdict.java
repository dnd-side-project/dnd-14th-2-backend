package com.example.demo.application.dto;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.Verdict;
import com.example.demo.domain.VerdictType;

public record MyVerdict(
    Long id,
    LedgerEntryInfo ledgerEntryInfo,
    UserInfo juror,
    VerdictType verdictType
) {
    public static MyVerdict from(Verdict verdict) {
        LedgerEntry ledgerEntry = verdict.getLedgerEntry();

        return new MyVerdict(
            verdict.getId(),
            LedgerEntryInfo.from(ledgerEntry),
            UserInfo.from(verdict.getJuror()),
            verdict.getType()
        );
    }
}
