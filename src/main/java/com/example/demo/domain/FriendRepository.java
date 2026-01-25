package com.example.demo.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface FriendRepository extends Repository<Friend, Long> {

    Friend save(Friend friend);

    @Query("""
        select count(f) > 0
        from Friend f
        where f.memberA.id = LEAST(:memberAId, :memberBId)
          and f.memberB.id = GREATEST(:memberAId, :memberBId)
    """)
    boolean existsFriend(Long memberAId, Long memberBId);
}
