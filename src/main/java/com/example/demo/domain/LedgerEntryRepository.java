package com.example.demo.domain;

import com.example.demo.application.dto.DailySumRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends Repository<LedgerEntry, Long> {

    LedgerEntry save(LedgerEntry entry);

    Optional<LedgerEntry> findByIdAndUser_Id(Long id, Long userId);

    void delete(LedgerEntry entry);

    @Query("""
            select new com.example.demo.application.dto.DailySumRow(
                l.occurredOn,
                coalesce(sum(case when l.type = 'INCOME' then l.amount else 0 end), 0),
                coalesce(sum(case when l.type = 'EXPENSE' then l.amount else 0 end), 0)
            )
            from LedgerEntry l
            where l.user.id = :userId and l.occurredOn between :start and :end
            group by l.occurredOn
            order by l.occurredOn asc
        """)
    List<DailySumRow> findDailySums(@Param("userId") Long userId,
                                    @Param("start") LocalDate start,
                                    @Param("end") LocalDate end);

    List<LedgerEntry> findAllByUser_IdAndOccurredOn(Long userId, LocalDate targetDate);

    Optional<LedgerEntry> findById(Long ledgerId);
}
