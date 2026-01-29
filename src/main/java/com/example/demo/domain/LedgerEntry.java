package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerCategory category;

    @Column(nullable = false, length = 15)
    private String description;

    @Column(nullable = false)
    private LocalDate occurredOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public LedgerEntry(Long amount,
                       LedgerType type,
                       LedgerCategory category,
                       String description,
                       LocalDate occurredOn,
                       PaymentMethod paymentMethod,
                       String memo, User user
    ) {
        this.amount = validateAmount(amount);
        this.type = type;
        this.category = category;
        this.description = normalizeDescription(description);
        this.occurredOn = occurredOn;
        this.paymentMethod = paymentMethod;
        this.memo = validateMemo(memo);
        this.user = user;
    }

    public void updateMemo(String memo) {
        this.memo = validateMemo(memo);
    }

    public void update(
        Long amount,
        LedgerType type,
        LedgerCategory category,
        String description,
        PaymentMethod paymentMethod,
        String memo
    ) {
        this.amount = validateAmount(amount);
        this.type = type;
        this.category = category;
        this.description = normalizeDescription(description);
        this.paymentMethod = paymentMethod;
        this.memo = validateMemo(memo);
    }

    private static long validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액(amount)은 0보다 커야 합니다.");
        }
        return amount;
    }

    private static String normalizeDescription(String rawDescription) {
        if (rawDescription == null) {
            throw new IllegalArgumentException("설명(description)은 필수입니다.");
        }
        String trimmed = rawDescription.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("설명(description)은 빈 문자열일 수 없습니다.");
        }
        if (trimmed.length() > 15) {
            throw new IllegalArgumentException("설명(description)은 1자 이상 15자 이내여야 합니다.");
        }
        return trimmed;
    }

    private static String validateMemo(String rawMemo) {
        if (rawMemo == null || rawMemo.isBlank()) {
            return rawMemo;
        }
        String trimmed = rawMemo.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("메모는 100자 이내여야 합니다.");
        }
        return trimmed;
    }
}
