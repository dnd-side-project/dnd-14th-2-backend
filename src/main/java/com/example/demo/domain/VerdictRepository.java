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
        select v FROM Verdict v 
        JOIN FETCH v.ledgerEntry 
        WHERE v.ledgerEntry.user.id = :userId
        """)
    List<Verdict> findMyVerdicts(@Param("userId") Long userId);

    @Query("""
        SELECT v FROM Verdict v 
        JOIN FETCH v.juror
        JOIN FETCH v.ledgerEntry 
        WHERE v.juror.id = :userId
        """)
    List<Verdict> findJurorVerdicts(@Param("userId") Long userId);

    @Query("""
        SELECT new com.example.demo.domain.FriendVerdictCount(v.juror.id, COUNT(v))
        FROM Verdict v
        WHERE v.ledgerEntry.user.id = :userId
        GROUP BY v.juror.id
        """)
    List<FriendVerdictCount> countVerdictsAsOwner(@Param("userId") Long userId);

    @Query("""
        SELECT new com.example.demo.domain.FriendVerdictCount(v.ledgerEntry.user.id, COUNT(v))
        FROM Verdict v
        WHERE v.juror.id = :userId
        GROUP BY v.ledgerEntry.user.id
        """)
    List<FriendVerdictCount> countVerdictsAsJuror(@Param("userId") Long userId);
}
