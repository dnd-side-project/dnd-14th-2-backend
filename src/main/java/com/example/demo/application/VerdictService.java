package com.example.demo.application;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.Mate;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.Verdict;
import com.example.demo.domain.VerdictRepository;
import com.example.demo.domain.VerdictType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerdictService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final MateRepository mateRepository;
    private final VerdictRepository verdictRepository;

    @Transactional
    public void requestVerdict(Long ledgerId, Long userId) {
        LedgerEntry ledgerEntry = ledgerEntryRepository.findByIdAndUser_Id(ledgerId, userId)
            .orElseThrow(() -> new IllegalArgumentException("해당되는 가계부 항목이 존재하지 않습니다."));

        List<Mate> acceptedMates = mateRepository.findAcceptedMates(userId);
        acceptedMates.stream()
            .map(ledgerEntry::requestVerdict)
            .forEach(verdictRepository::save);
    }

    @Transactional
    public void judge(Long verdictId, Long jurorId, VerdictType verdictType) {
        Verdict verdict = verdictRepository.findById(verdictId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판결입니다."));

        User juror = userRepository.findById(jurorId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        verdict.judge(juror, verdictType);
    }
}
