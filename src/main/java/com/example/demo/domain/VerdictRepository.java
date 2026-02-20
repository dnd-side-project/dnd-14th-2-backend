package com.example.demo.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface VerdictRepository extends Repository<Verdict, Long> {

    Optional<Verdict> findById(Long verdictId);
}
