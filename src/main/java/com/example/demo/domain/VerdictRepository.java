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

    @Query("""
        SELECT new com.example.demo.domain.FriendVerdictCount(
            CASE WHEN v.ledgerEntry.user.id = :userId THEN v.juror.id
                 ELSE v.ledgerEntry.user.id END,
            COUNT(v)
        )
        FROM Verdict v
        WHERE v.ledgerEntry.user.id = :userId OR v.juror.id = :userId
        GROUP BY CASE WHEN v.ledgerEntry.user.id = :userId THEN v.juror.id
                      ELSE v.ledgerEntry.user.id END
        """)
    List<FriendVerdictCount> countVerdictsByFriend(@Param("userId") Long userId);
}
