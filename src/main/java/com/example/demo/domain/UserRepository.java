package com.example.demo.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findById(Long userId);
}
