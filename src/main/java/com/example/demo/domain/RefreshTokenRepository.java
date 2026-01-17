package com.example.demo.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RefreshTokenRepository extends Repository<RefreshToken, Long> {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
