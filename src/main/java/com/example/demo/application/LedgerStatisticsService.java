package com.example.demo.application;

import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerStatisticsService {

    private final Clock clock;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerStatisticsResponse getMonthlyStatistics(Long userId, LedgerType type) {
        YearMonth currentMonth = YearMonth.now(clock);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        // 이번달 전체 내역 조회
        Map<LedgerCategory, Long> categoryAmounts = fetchCurrentMonthCategoryAmounts(
            userId, type, currentMonth
        );

        // 이번달 전체 내역으로 합계 계산
        long currentMonthTotalAmount = calculateTotalAmount(categoryAmounts);

        // 지난달 전체 합계 조회
        long lastMonthTotalAmount = findTotalAmount(userId, type, lastMonth);

        return new LedgerStatisticsResponse(
            type,
            categoryAmounts,
            currentMonthTotalAmount,
            lastMonthTotalAmount,
            currentMonth.atDay(1),
            currentMonth.atEndOfMonth()
        );
    }

    private long findTotalAmount(Long userId, LedgerType type, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            userId,
            type,
            startDate,
            endDate
        );
    }

    private Map<LedgerCategory, Long> fetchCurrentMonthCategoryAmounts(
        Long userId,
        LedgerType type,
        YearMonth currentMonth
    ) {
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAllByUserAndTypeAndOccurredOnBetween(
            userId,
            type,
            startDate,
            endDate
        );
        return toCategoryAmountResponses(ledgerEntries);
    }

    private Long calculateTotalAmount(Map<LedgerCategory, Long> categoryAmounts) {
        return categoryAmounts.values().stream()
            .mapToLong(value -> value)
            .sum();
    }

    private Map<LedgerCategory, Long> toCategoryAmountResponses(List<LedgerEntry> ledgerEntries) {
        return ledgerEntries.stream()
            .collect(Collectors.groupingBy(
                LedgerEntry::getCategory,
                Collectors.summingLong(LedgerEntry::getAmount)
            ));
    }
}
