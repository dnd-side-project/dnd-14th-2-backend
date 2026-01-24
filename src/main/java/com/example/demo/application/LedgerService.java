package com.example.demo.application;

import com.example.demo.application.dto.*;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LedgerService {
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;

    @Transactional
    public LedgerResult createLedgerEntry(UpsertLedgerCommand command) {
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
        return LedgerResult.from(saved);
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
            .collect(Collectors.toMap(DailySumRow::date, r -> r));

        List<DailySummary> daily = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DailySumRow row = byDate.get(date);
            long income = (row == null) ? 0L : row.incomeTotal();
            long expense = (row == null) ? 0L : row.expenseTotal();
            daily.add(new DailySummary(date, income, expense));
        }

        return daily;
    }

    @Transactional(readOnly = true)
    public DailyLedgerDetail getLedgerEntriesByDate(Long userId, LocalDate targetDate) {
        List<LedgerResult> results = ledgerEntryRepository.findAllByUser_IdAndOccurredOn(userId, targetDate).stream().map(LedgerResult::from).toList();

        long incomeTotal = results.stream()
            .filter(r -> r.type() == com.example.demo.domain.enums.LedgerType.INCOME)
            .mapToLong(LedgerResult::amount)
            .sum();

        long expenseTotal = results.stream()
            .filter(r -> r.type() == com.example.demo.domain.enums.LedgerType.EXPENSE)
            .mapToLong(LedgerResult::amount)
            .sum();

        return new DailyLedgerDetail(targetDate, incomeTotal, expenseTotal, results);
    }

}
