package com.example.demo.application;

import com.example.demo.application.dto.MateInfo;
import com.example.demo.application.dto.MateReceivedInfo;
import com.example.demo.domain.Mate;
import com.example.demo.domain.FriendVerdictCount;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.VerdictRepository;
import com.example.demo.domain.enums.MateStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final VerdictRepository verdictRepository;

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

    @Transactional(readOnly = true)
    public List<MateInfo> getAcceptedMates(Long userId) {
        Map<Long, Long> verdictCounts = Stream.concat(
                verdictRepository.countVerdictsAsOwner(userId).stream(),
                verdictRepository.countVerdictsAsJuror(userId).stream()
            ).collect(Collectors.toMap(
                FriendVerdictCount::friendId,
                FriendVerdictCount::count,
                Long::sum
            ));

        return mateRepository.findAllAcceptedWithFriend(userId).stream()
            .map(result -> new MateInfo(
                result.mate().getId(),
                result.friend().getNickname(),
                result.friend().getInvitationCode().value(),
                verdictCounts.getOrDefault(result.friend().getId(), 0L).intValue()
            ))
            .toList();
    }

    @Transactional
    public void updateMateStatus(Long mateId, Long userId, MateStatus status) {
        Mate mate = mateRepository.findById(mateId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        if (!mate.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("친구 요청의 수신자만 수락/거절할 수 있습니다.");
        }

        switch (status) {
            case ACCEPTED -> mate.accept();
            case REJECTED -> mate.reject();
            default -> throw new IllegalArgumentException("친구요청은 수락 또는 거절로만 변경 가능합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<MateReceivedInfo> getReceivedRequests(Long userId) {
        return mateRepository.findAllPendingByReceiverId(userId).stream()
            .map(mate -> new MateReceivedInfo(
                mate.getId(),
                mate.getRequester().getNickname(),
                mate.getRequester().getInvitationCode().value()
            ))
            .toList();
    }
}
