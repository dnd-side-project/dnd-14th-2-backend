package com.example.demo.domain;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface LedgerRepository extends Repository<LedgerEntry, Long> {

    LedgerEntry save(LedgerEntry entry);

    Optional<LedgerEntry> findByIdAndUserId(Long id, Long userId);

    void delete(LedgerEntry entry);
}
