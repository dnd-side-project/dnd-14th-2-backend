package com.example.demo.application;

import com.example.demo.application.dto.CategoryAmountDto;
import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.enums.LedgerType;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerStatisticsService {

    private final Clock clock;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerStatisticsResponse getMonthlyStatistics(Long userId, LedgerType type) {
        YearMonth currentMonth = YearMonth.now(clock);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        List<CategoryAmountDto> categoryAmounts = fetchCurrentMonthCategoryAmounts(
            userId, type, currentMonth
        );

        long currentMonthTotalAmount = calculateTotalAmount(categoryAmounts);
        long lastMonthTotalAmount = findTotalAmount(userId, type, lastMonth);

        Map<String, Long> categoryMap = buildCategoryMap(categoryAmounts);

        return new LedgerStatisticsResponse(
            type,
            categoryMap,
            currentMonthTotalAmount,
            lastMonthTotalAmount
        );
    }

    private long findTotalAmount(Long userId, LedgerType type, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        Long total = ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            userId,
            type,
            startDate,
            endDate
        );

        return Objects.requireNonNullElse(total, 0L);
    }

    private List<CategoryAmountDto> fetchCurrentMonthCategoryAmounts(
        Long userId,
        LedgerType type,
        YearMonth currentMonth
    ) {
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            userId,
            type,
            startDate,
            endDate
        );
    }

    private Long calculateTotalAmount(List<CategoryAmountDto> categoryAmounts) {
        return categoryAmounts.stream()
            .mapToLong(CategoryAmountDto::totalAmount)
            .sum();
    }

    private Map<String, Long> buildCategoryMap(List<CategoryAmountDto> categoryAmounts) {
        return categoryAmounts.stream()
            .collect(Collectors.toMap(
                dto -> dto.category().name(),
                CategoryAmountDto::totalAmount
            ));
    }

}
