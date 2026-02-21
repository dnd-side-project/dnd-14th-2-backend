package com.example.demo.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface VerdictRepository extends Repository<Verdict, Long> {

    void saveAll(List<Verdict> verdicts);

    Optional<Verdict> findById(Long verdictId);
}
