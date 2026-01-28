package com.example.demo.application;

import com.example.demo.application.dto.*;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.domain.enums.LedgerType.EXPENSE;
import static com.example.demo.domain.enums.LedgerType.INCOME;

@RequiredArgsConstructor
@Service
public class LedgerService {
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createLedgerEntry(UpsertLedgerCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        LedgerEntry entry = new LedgerEntry(
            command.amount(),
            command.type(),
            command.category(),
            command.description(),
            command.occurredOn(),
            command.paymentMethod(),
            command.memo(),
            user
        );

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public LedgerResult getLedgerEntry(Long userId, Long ledgerId) {
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUser_Id(ledgerId, userId)
            .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        return LedgerResult.from(entry);
    }

    @Transactional
    public void updateLedgerMemo(Long userId, Long ledgerId, String memo) {
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUser_Id(ledgerId, userId)
            .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        entry.updateMemo(memo);
    }

    @Transactional
    public LedgerResult updateLedgerEntry(Long ledgerId, UpsertLedgerCommand command) {
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUser_Id(ledgerId, command.userId())
            .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        entry.update(
            command.amount(),
            command.type(),
            command.category(),
            command.description(),
            command.paymentMethod(),
            command.memo()
        );

        return LedgerResult.from(entry);
    }

    @Transactional
    public void deleteLedgerEntry(Long userId, Long ledgerId) {
        ledgerEntryRepository.findByIdAndUser_Id(ledgerId, userId)
            .ifPresentOrElse(
                ledgerEntryRepository::delete,
                () -> {
                    throw new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다.");
                }
            );
    }

    @Transactional(readOnly = true)
    public List<DailySummary> getSummary(Long userId, LocalDate start, LocalDate end) {
        List<DailySumRow> rows = ledgerEntryRepository.findDailySums(userId, start, end);

        Map<LocalDate, DailySumRow> byDate = rows.stream()
            .collect(Collectors.toMap(DailySumRow::date, row -> row));

        return start.datesUntil(end.plusDays(1))
            .map(date -> Optional.ofNullable(byDate.get(date))
                .map(row -> DailySummary.of(date, row))
                .orElseGet(() -> DailySummary.of(date))
            ).toList();
    }

    @Transactional(readOnly = true)
    public DailyLedgerDetail getLedgerEntriesByDate(Long userId, LocalDate targetDate) {
        List<LedgerResult> results = ledgerEntryRepository.findAllByUser_IdAndOccurredOn(userId, targetDate)
            .stream()
            .map(LedgerResult::from)
            .toList();

        long incomeTotal = getTotalByType(results, INCOME);
        long expenseTotal = getTotalByType(results, EXPENSE);

        return new DailyLedgerDetail(targetDate, incomeTotal, expenseTotal, results);
    }

    private static long getTotalByType(List<LedgerResult> results, LedgerType expense) {
        return results.stream()
            .filter(r -> r.type() == expense)
            .mapToLong(LedgerResult::amount)
            .sum();
    }
}
