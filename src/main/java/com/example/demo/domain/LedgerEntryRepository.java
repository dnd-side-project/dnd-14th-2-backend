package com.example.demo.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

public interface LedgerEntryRepository extends Repository<LedgerEntry, Long> {

    LedgerEntry save(LedgerEntry entry);

    Optional<LedgerEntry> findByIdAndUser_Id(Long id, Long userId);

    void delete(LedgerEntry entry);

    List<LedgerEntry> findAllByUser_IdAndOccurredOnBetween(
        Long userId, LocalDate start, LocalDate end, Sort sort
    );

    Optional<LedgerEntry> findById(Long ledgerId);
}
