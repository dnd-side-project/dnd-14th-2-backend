package com.example.demo.domain;

import com.example.demo.domain.enums.MateStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MateRepository extends Repository<Mate, Long> {

    Mate save(Mate mate);

    Optional<Mate> findById(Long id);

    @Query("""
        select m from Mate m join fetch m.requester join fetch m.receiver
             where (m.requester.id = :id or m.receiver.id = :id) and m.status = :status
        """)
    List<Mate> findMates(@Param("id") Long userId, @Param("status") MateStatus mateStatus);

    @Query("SELECT COUNT(m) > 0 FROM Mate m " +
        "WHERE ((m.requester.id = :user1Id AND m.receiver.id = :user2Id) " +
        "OR (m.requester.id = :user2Id AND m.receiver.id = :user1Id)) " +
        "AND m.status IN ('PENDING', 'ACCEPTED')")
    boolean existsMateBetween(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT new com.example.demo.domain.MateWithFriend(m, f) " +
        "FROM Mate m " +
        "JOIN User f ON (m.requester.id = :userId AND f.id = m.receiver.id) " +
        "            OR (m.receiver.id = :userId AND f.id = m.requester.id) " +
        "WHERE m.status = 'ACCEPTED' " +
        "AND (m.requester.id = :userId OR m.receiver.id = :userId)")
    List<MateWithFriend> findAllAcceptedWithFriend(@Param("userId") Long userId);

    @Query("SELECT m FROM Mate m JOIN FETCH m.requester " +
        "WHERE m.receiver.id = :userId AND m.status = 'PENDING'")
    List<Mate> findAllPendingByReceiverId(@Param("userId") Long userId);
}
