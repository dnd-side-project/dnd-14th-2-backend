package com.example.demo.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface VerdictRepository extends Repository<Verdict, Long> {

    Verdict save(Verdict verdict);

    Optional<Verdict> findById(Long verdictId);

    List<Verdict> findAllByLedgerEntry_Id(Long ledgerEntryId);
}
