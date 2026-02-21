package com.example.demo.application;

import com.example.demo.domain.Mate;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long requestMate(Long userId, String invitationCode) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        User receiver = userRepository.findByInvitationCode_Value(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 초대코드입니다."));

        if (mateRepository.existsMateBetween(requester.getId(), receiver.getId())) {
            throw new IllegalArgumentException("이미 친구 관계가 존재하거나 요청 대기 중입니다.");
        }

        return mateRepository.save(new Mate(requester, receiver)).getId();
    }
}
