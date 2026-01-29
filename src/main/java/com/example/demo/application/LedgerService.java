package com.example.demo.application;

import com.example.demo.application.dto.*;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    public List<LedgerResult> getSummary(Long userId, LocalDate start, LocalDate end) {
        List<LedgerEntry> entries = ledgerEntryRepository.findAllByUser_IdAndOccurredOnBetween(
            userId, start, end, Sort.by(Sort.Order.asc("occurredOn"), Sort.Order.asc("id"))
        );

        return entries.stream()
            .map(LedgerResult::from)
            .toList();
    }
}
