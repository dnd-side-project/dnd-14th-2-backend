package com.example.demo.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface VerdictRepository extends Repository<Verdict, Long> {

    Verdict save(Verdict verdict);

    Optional<Verdict> findById(Long verdictId);

    List<Verdict> findAllByLedgerEntry_Id(Long ledgerEntryId);

    @Query("""
        select v from Verdict v where v.ledgerEntry.user.id = :userId
        """)
    List<Verdict> findMyVerdicts(@Param("userId") Long userId);

    @Query("""
        select v from Verdict v where v.juror.id = :userId
        """)
    List<Verdict> findJurorVerdicts(@Param("userId") Long userId);
}
