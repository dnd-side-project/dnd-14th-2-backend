package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verdict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id", referencedColumnName = "id")
    private LedgerEntry ledgerEntry;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juror_id", referencedColumnName = "id")
    private User juror;

    private VerdictType type;

    public Verdict(LedgerEntry ledgerEntry, User juror) {
        this.ledgerEntry = ledgerEntry;
        this.juror = juror;
    }

    public void judge(User juror, VerdictType type) {
        validateIsNotSelfJudge(juror);
        validateIsValidJuror(juror);
        validateIsNotCompletedJudge();

        this.type = type;
    }

    private void validateIsNotCompletedJudge() {
        if (!isPending()) {
            throw new IllegalStateException("이미 판결된 심판입니다.");
        }
    }

    private void validateIsValidJuror(User juror) {
        if (!this.juror.equals(juror)) {
            throw new IllegalStateException("판결 권한이 없습니다.");
        }
    }

    private void validateIsNotSelfJudge(User juror) {
        if (this.ledgerEntry.getUser().equals(juror)) {
            throw new IllegalStateException("본인의 소비 심판을 판결할 수 없습니다.");
        }
    }

    public boolean isPending() {
        return type == null;
    }

    public Long getId() {
        return id;
    }
}
