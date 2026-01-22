package com.example.demo.application;

import com.example.demo.application.dto.CreateLedgerCommand;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    @Transactional
    public LedgerResult createLedgerEntry(CreateLedgerCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LedgerEntry entry = new LedgerEntry(
                command.amount(),
                command.type(),
                command.category(),
                command.description(),
                command.occurredOn(),
                command.paymentMethod(),
                user
        );

        LedgerEntry saved = ledgerRepository.save(entry);
        return LedgerResult.from(saved);
    }

    @Transactional(readOnly = true)
    public LedgerResult getLedgerEntry(Long userId, Long ledgerId) {
        LedgerEntry entry = ledgerRepository.findByIdAndUserId(ledgerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        return LedgerResult.from(entry);
    }

    public void updateLedgerMemo(Long userId, Long ledgerId, String memo) {
        LedgerEntry entry = ledgerRepository.findByIdAndUserId(ledgerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        entry.updateMemo(memo);
    }

    @Transactional
    public LedgerResult updateLedgerEntry(Long ledgerId, CreateLedgerCommand command) {
        LedgerEntry entry = ledgerRepository.findByIdAndUserId(ledgerId, command.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));
        entry.update(
                command.amount(),
                command.type(),
                command.category(),
                command.description(),
                command.paymentMethod()
        );

        return LedgerResult.from(entry);
    }

    @Transactional
    public void deleteLedgerEntry(Long userId, Long ledgerId) {
        ledgerRepository.findByIdAndUserId(ledgerId, userId)
                .ifPresentOrElse(
                        ledgerRepository::delete,
                        () -> {
                            throw new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다.");
                        }
                );

    }

}
