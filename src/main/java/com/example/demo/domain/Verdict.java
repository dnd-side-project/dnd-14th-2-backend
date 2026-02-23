package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Verdict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id", referencedColumnName = "id", nullable = false)
    private LedgerEntry ledgerEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juror_id", referencedColumnName = "id", nullable = false)
    private User juror;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private VerdictType type;

    public Verdict(LedgerEntry ledgerEntry, User juror) {
        this.ledgerEntry = ledgerEntry;
        this.juror = juror;
        this.type = VerdictType.PENDING;
    }

    public void judge(User juror, VerdictType type) {
        validateIsValidVerdictType(type);
        validateIsNotCompletedJudge();
        validateIsValidJuror(juror);

        this.type = type;
    }

    private void validateIsValidVerdictType(VerdictType type) {
        if (type.equals(VerdictType.PENDING)) {
            throw new IllegalArgumentException("판결에는 PENDING을 사용할 수 없습니다.");
        }
    }

    private void validateIsNotCompletedJudge() {
        if (!isPending()) {
            throw new IllegalArgumentException("이미 판결된 심판입니다.");
        }
    }

    private void validateIsValidJuror(User juror) {
        if (!this.juror.equals(juror)) {
            throw new IllegalArgumentException("판결 권한이 없습니다.");
        }
    }

    public boolean isPending() {
        return type == VerdictType.PENDING;
    }
}
