package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.LedgerEntryInfo;
import com.example.demo.application.dto.MyVerdict;
import com.example.demo.domain.VerdictType;

public record MyVerdictWebResponse(
    Long id,
    LedgerEntryInfoWebResponse ledgerEntryInfo,
    UserInfoWebResponse juror,
    VerdictType verdictType
) {
    public static MyVerdictWebResponse from(MyVerdict myVerdict) {
        LedgerEntryInfo ledgerEntryInfo = myVerdict.ledgerEntryInfo();

        return new MyVerdictWebResponse(
            myVerdict.id(),
            new LedgerEntryInfoWebResponse(
                ledgerEntryInfo.id(),
                ledgerEntryInfo.amount(),
                ledgerEntryInfo.category(),
                ledgerEntryInfo.paymentMethod(),
                ledgerEntryInfo.description()
            ),
            UserInfoWebResponse.from(myVerdict.juror()),
            myVerdict.verdictType()
        );
    }
}
