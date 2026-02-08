package com.example.demo.domain.enums;

import java.util.Optional;

public enum LedgerCategory {
    // Expense categories
    FOOD(LedgerType.EXPENSE),
    TRANSPORT(LedgerType.EXPENSE),
    HOUSING(LedgerType.EXPENSE),
    SHOPPING(LedgerType.EXPENSE),
    HEALTH_MEDICAL(LedgerType.EXPENSE),
    EDUCATION_SELF_DEVELOPMENT(LedgerType.EXPENSE),
    LEISURE_HOBBY(LedgerType.EXPENSE),
    SAVINGS_FINANCE(LedgerType.EXPENSE),

    // Income categories
    SALARY(LedgerType.INCOME),              // 월급
    SIDE_INCOME(LedgerType.INCOME),         // 부수입
    BONUS(LedgerType.INCOME),               // 상여
    ALLOWANCE(LedgerType.INCOME),           // 용돈
    PART_TIME(LedgerType.INCOME),           // 아르바이트
    FINANCIAL_INCOME(LedgerType.INCOME),    // 금융수입
    DUTCH_PAY(LedgerType.INCOME),           // 더치페이
    TRANSFER(LedgerType.INCOME),            // 이체

    OTHER(null);

    private final LedgerType fixedType;

    LedgerCategory(LedgerType fixedType) {
        this.fixedType = fixedType;
    }

    public Optional<LedgerType> fixedType() {
        return Optional.ofNullable(fixedType);
    }
}
