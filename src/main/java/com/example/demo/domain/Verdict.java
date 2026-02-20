package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verdict {

    @Id
    @GeneratedValue
    private Long id;

    private LedgerEntry ledgerEntry;

    private User juror;

    private VerdictType type;

    public Verdict(LedgerEntry ledgerEntry, User juror) {
        this.ledgerEntry = ledgerEntry;
        this.juror = juror;
    }

    public void judge(User juror, VerdictType type) {
        if (!this.juror.equals(juror)) {
            throw new IllegalStateException("판결 권한이 없습니다.");
        }

        if (!isPending()) {
            throw new IllegalStateException("이미 판결된 심판입니다.");
        }

        this.type = type;
    }

    public boolean isPending() {
        return type == null;
    }
}
