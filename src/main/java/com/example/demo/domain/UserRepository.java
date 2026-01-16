package com.example.demo.domain;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
