package com.example.demo.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findById(Long userId);

    @Query("""
        select count(u) > 0
        from User u
        where u.nickname.value = :nickname
    """)
    boolean existsByNickname(String nickname);
}
