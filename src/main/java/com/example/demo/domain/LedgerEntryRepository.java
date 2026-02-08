package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends Repository<LedgerEntry, Long> {

    LedgerEntry save(LedgerEntry entry);

    Optional<LedgerEntry> findByIdAndUser_Id(Long id, Long userId);

    void delete(LedgerEntry entry);

    List<LedgerEntry> findAllByUser_IdAndOccurredOnBetween(
        Long userId, LocalDate start, LocalDate end, Sort sort
    );

    Optional<LedgerEntry> findById(Long ledgerId);

    @Query("""
            SELECT le
            FROM LedgerEntry le
            WHERE le.user.id = :userId
                AND le.type = :type
                AND le.occurredOn BETWEEN :startDate AND :endDate
        """)
    List<LedgerEntry> findAllByUserAndTypeAndOccurredOnBetween(
        @Param("userId") Long userId,
        @Param("type") LedgerType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COALESCE(SUM(le.amount), 0)
            FROM LedgerEntry le
            WHERE le.user.id = :userId
                AND le.type = :type
                AND le.occurredOn >= :startDate
                AND le.occurredOn <= :endDate
        """)
    Long findTotalAmountByUserAndTypeAndPeriod(
        @Param("userId") Long userId,
        @Param("type") LedgerType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
