package com.example.demo.application;

import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.Verdict;
import com.example.demo.domain.VerdictRepository;
import com.example.demo.domain.VerdictType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerdictService {

    private final UserRepository userRepository;
    private final VerdictRepository verdictRepository;

    public void judge(Long verdictId, Long jurorId, VerdictType verdictType) {
        Verdict verdict = verdictRepository.findById(verdictId)
            .orElseThrow();

        User juror = userRepository.findById(jurorId)
            .orElseThrow();

        verdict.judge(juror, verdictType);
    }
}
