package com.example.demo.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MateRepository extends Repository<Mate, Long> {

    Mate save(Mate mate);

    Optional<Mate> findById(Long id);

    @Query("SELECT COUNT(m) > 0 FROM Mate m " +
            "WHERE ((m.requester.id = :user1Id AND m.receiver.id = :user2Id) " +
            "OR (m.requester.id = :user2Id AND m.receiver.id = :user1Id)) " +
            "AND m.status IN ('PENDING', 'ACCEPTED')")
    boolean existsMateBetween(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}
