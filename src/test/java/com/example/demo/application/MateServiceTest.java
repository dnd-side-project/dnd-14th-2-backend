package com.example.demo.application;

import com.example.demo.application.dto.MateInfo;
import com.example.demo.application.dto.MateReceivedInfo;
import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.Mate;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.VerdictRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.MateStatus;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.demo.util.DbUtils.givenSavedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MateServiceTest extends AbstractIntegrationTest {

    @Autowired
    MateService sut;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MateRepository mateRepository;

    @Autowired
    VerdictRepository verdictRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Nested
    @DisplayName("친구 요청")
    class RequestMate {

        @Test
        void 정상적으로_친구_요청이_생성된다() {
            // given
            User requester = givenSavedUser(userRepository);
            User receiver = givenSavedUser(userRepository, new Nickname("친구"), new InvitationCode("ABCDEF"));

            // when
            Long mateId = sut.requestMate(requester.getId(), "ABCDEF");

            // then
            assertThat(mateId).isNotNull();
            var mate = mateRepository.findById(mateId).orElseThrow();
            assertThat(mate.getRequester().getId()).isEqualTo(requester.getId());
            assertThat(mate.getReceiver().getId()).isEqualTo(receiver.getId());
            assertThat(mate.getStatus()).isEqualTo(MateStatus.PENDING);
        }

        @Test
        void 존재하지_않는_초대코드이면_예외가_발생한다() {
            // given
            User requester = givenSavedUser(userRepository);

            // when & then
            assertThatThrownBy(() -> sut.requestMate(requester.getId(), "ZZZZZZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 초대코드입니다.");
        }

        @Test
        void 자기_자신의_초대코드로_요청하면_예외가_발생한다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("나자신"), new InvitationCode("MYSELF"));

            // when & then
            assertThatThrownBy(() -> sut.requestMate(requester.getId(), "MYSELF"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        @Test
        void 이미_A에서_B로_관계가_존재하면_예외가_발생한다() {
            // given
            User requester = givenSavedUser(userRepository);
            User receiver = givenSavedUser(userRepository, new Nickname("친구"), new InvitationCode("ABCDEF"));
            sut.requestMate(requester.getId(), "ABCDEF");

            // when & then
            assertThatThrownBy(() -> sut.requestMate(requester.getId(), "ABCDEF"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 친구 관계가 존재하거나 요청 대기 중입니다.");
        }

        @Test
        void 이미_B에서_A로_관계가_존재하면_예외가_발생한다() {
            // given
            User userA = givenSavedUser(userRepository, new Nickname("철수"), new InvitationCode("AAAAAA"));
            User userB = givenSavedUser(userRepository, new Nickname("영희"), new InvitationCode("BBBBBB"));
            sut.requestMate(userB.getId(), "AAAAAA"); // B→A 요청

            // when & then
            assertThatThrownBy(() -> sut.requestMate(userA.getId(), "BBBBBB")) // A→B 요청
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 친구 관계가 존재하거나 요청 대기 중입니다.");
        }
    }

    @Nested
    @DisplayName("친구 목록 조회")
    class GetAcceptedMates {

        @Test
        void ACCEPTED_친구만_반환된다() {
            // given
            User me = givenSavedUser(userRepository);
            User friend1 = givenSavedUser(userRepository, new Nickname("친구1"), new InvitationCode("FFAAAA"));
            User friend2 = givenSavedUser(userRepository, new Nickname("친구2"), new InvitationCode("FFBBBB"));
            User pending = givenSavedUser(userRepository, new Nickname("대기자"), new InvitationCode("PPPPPP"));

            Long mateId1 = sut.requestMate(me.getId(), "FFAAAA");
            Long mateId2 = sut.requestMate(me.getId(), "FFBBBB");
            sut.requestMate(me.getId(), "PPPPPP"); // PENDING 상태 유지

            acceptMate(mateId1);
            acceptMate(mateId2);

            // when
            List<MateInfo> result = sut.getAcceptedMates(me.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(MateInfo::nickname)
                .containsExactlyInAnyOrder("친구1", "친구2");
        }

        @Test
        void 내가_requester이면_receiver를_friend로_반환한다() {
            // given
            User me = givenSavedUser(userRepository);
            User friend = givenSavedUser(userRepository, new Nickname("상대방"), new InvitationCode("FFFFFF"));
            Long mateId = sut.requestMate(me.getId(), "FFFFFF");
            acceptMate(mateId);

            // when
            List<MateInfo> result = sut.getAcceptedMates(me.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("상대방");
            assertThat(result.get(0).invitationCode()).isEqualTo("FFFFFF");
        }

        @Test
        void 친구와_함께한_심판_횟수가_반환된다() {
            // given
            User me = givenSavedUser(userRepository);
            User friend1 = givenSavedUser(userRepository, new Nickname("친구1"), new InvitationCode("FFAAAA"));
            User friend2 = givenSavedUser(userRepository, new Nickname("친구2"), new InvitationCode("FFBBBB"));

            Long mateId1 = sut.requestMate(me.getId(), "FFAAAA");
            Long mateId2 = sut.requestMate(me.getId(), "FFBBBB");
            acceptMate(mateId1);
            acceptMate(mateId2);

            // 내 가계부 항목에 friend1이 배심원인 심판 2건
            LedgerEntry myEntry1 = ledgerEntryRepository.save(
                new LedgerEntry(10000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "점심", LocalDate.now(), PaymentMethod.CASH, null, me));
            verdictRepository.save(myEntry1.requestVerdict(friend1));

            LedgerEntry myEntry2 = ledgerEntryRepository.save(
                new LedgerEntry(20000L, LedgerType.EXPENSE, LedgerCategory.SHOPPING, "옷", LocalDate.now(), PaymentMethod.CREDIT_CARD, null, me));
            verdictRepository.save(myEntry2.requestVerdict(friend1));

            // friend1의 가계부 항목에 내가 배심원인 심판 1건
            LedgerEntry friendEntry = ledgerEntryRepository.save(
                new LedgerEntry(5000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "커피", LocalDate.now(), PaymentMethod.CASH, null, friend1));
            verdictRepository.save(friendEntry.requestVerdict(me));

            // when
            List<MateInfo> result = sut.getAcceptedMates(me.getId());

            // then
            assertThat(result).hasSize(2);
            MateInfo friend1Info = result.stream()
                .filter(m -> m.nickname().equals("친구1")).findFirst().orElseThrow();
            MateInfo friend2Info = result.stream()
                .filter(m -> m.nickname().equals("친구2")).findFirst().orElseThrow();
            assertThat(friend1Info.verdictCount()).isEqualTo(3);
            assertThat(friend2Info.verdictCount()).isEqualTo(0);
        }

        @Test
        void 내가_receiver이면_requester를_friend로_반환한다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User me = givenSavedUser(userRepository, new Nickname("나"), new InvitationCode("MMMMMM"));
            Long mateId = sut.requestMate(requester.getId(), "MMMMMM");
            acceptMate(mateId);

            // when
            List<MateInfo> result = sut.getAcceptedMates(me.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("요청자");
            assertThat(result.get(0).invitationCode()).isEqualTo("RRRRRR");
        }
    }

    @Nested
    @DisplayName("받은 친구 요청 조회")
    class GetReceivedRequests {

        @Test
        void PENDING_상태의_받은_요청만_반환된다() {
            // given
            User me = givenSavedUser(userRepository);
            User sender1 = givenSavedUser(userRepository, new Nickname("보낸사람1"), new InvitationCode("AAAAAA"));
            User sender2 = givenSavedUser(userRepository, new Nickname("보낸사람2"), new InvitationCode("BBBBBB"));
            User sender3 = givenSavedUser(userRepository, new Nickname("수락됨"), new InvitationCode("CCCCCC"));

            sut.requestMate(sender1.getId(), me.getInvitationCode().value()); // PENDING
            sut.requestMate(sender2.getId(), me.getInvitationCode().value()); // PENDING
            Long acceptedMateId = sut.requestMate(sender3.getId(), me.getInvitationCode().value());
            acceptMate(acceptedMateId); // ACCEPTED

            // when
            var result = sut.getReceivedRequests(me.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(MateReceivedInfo::nickname)
                .containsExactlyInAnyOrder("보낸사람1", "보낸사람2");
        }

        @Test
        void 내가_보낸_요청은_반환되지_않는다() {
            // given
            User me = givenSavedUser(userRepository);
            User other = givenSavedUser(userRepository, new Nickname("상대방"), new InvitationCode("OOOOOO"));
            sut.requestMate(me.getId(), "OOOOOO"); // 내가 보낸 요청

            // when
            var result = sut.getReceivedRequests(me.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 요청자의_닉네임과_초대코드가_포함된다() {
            // given
            User me = givenSavedUser(userRepository);
            User sender = givenSavedUser(userRepository, new Nickname("친구"), new InvitationCode("FFFFFF"));
            sut.requestMate(sender.getId(), me.getInvitationCode().value());

            // when
            var result = sut.getReceivedRequests(me.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("친구");
            assertThat(result.get(0).invitationCode()).isEqualTo("FFFFFF");
            assertThat(result.get(0).mateId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("친구 요청 수락/거절")
    class UpdateMateStatus {

        @Test
        void 친구_요청을_수락하면_ACCEPTED_상태가_된다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User receiver = givenSavedUser(userRepository, new Nickname("수신자"), new InvitationCode("EEEEEE"));
            Long mateId = sut.requestMate(requester.getId(), "EEEEEE");

            // when
            sut.updateMateStatus(mateId, receiver.getId(), MateStatus.ACCEPTED);

            // then
            Mate mate = mateRepository.findById(mateId).orElseThrow();
            assertThat(mate.getStatus()).isEqualTo(MateStatus.ACCEPTED);
            assertThat(mate.getModifiedAt()).isNotNull();
        }

        @Test
        void 친구_요청을_거절하면_삭제된다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User receiver = givenSavedUser(userRepository, new Nickname("수신자"), new InvitationCode("EEEEEE"));
            Long mateId = sut.requestMate(requester.getId(), "EEEEEE");

            // when
            sut.updateMateStatus(mateId, receiver.getId(), MateStatus.REJECTED);

            // then: 거절된 요청은 삭제되어 찾을 수 없음
            assertThat(mateRepository.findById(mateId)).isEmpty();
        }

        @Test
        void 거절된_요청은_다시_보낼_수_있다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User receiver = givenSavedUser(userRepository, new Nickname("수신자"), new InvitationCode("EEEEEE"));
            Long firstMateId = sut.requestMate(requester.getId(), "EEEEEE");
            sut.updateMateStatus(firstMateId, receiver.getId(), MateStatus.REJECTED);

            // when: 다시 요청
            Long secondMateId = sut.requestMate(requester.getId(), "EEEEEE");

            // then: 새로운 요청이 생성됨
            assertThat(secondMateId).isNotNull().isNotEqualTo(firstMateId);
            Mate newMate = mateRepository.findById(secondMateId).orElseThrow();
            assertThat(newMate.getStatus()).isEqualTo(MateStatus.PENDING);
        }

        @Test
        void 존재하지_않는_Mate이면_예외가_발생한다() {
            // given
            User receiver = givenSavedUser(userRepository);

            // when & then
            assertThatThrownBy(() -> sut.updateMateStatus(999L, receiver.getId(), MateStatus.ACCEPTED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 친구 요청입니다.");
        }

        @Test
        void receiver가_아닌_사용자가_요청하면_예외가_발생한다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User receiver = givenSavedUser(userRepository, new Nickname("수신자"), new InvitationCode("EEEEEE"));
            User other = givenSavedUser(userRepository, new Nickname("다른사람"), new InvitationCode("OOOOOO"));
            Long mateId = sut.requestMate(requester.getId(), "EEEEEE");

            // when & then
            assertThatThrownBy(() -> sut.updateMateStatus(mateId, other.getId(), MateStatus.ACCEPTED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("친구 요청의 수신자만 수락/거절할 수 있습니다.");
        }

        @Test
        void 이미_처리된_요청이면_예외가_발생한다() {
            // given
            User requester = givenSavedUser(userRepository, new Nickname("요청자"), new InvitationCode("RRRRRR"));
            User receiver = givenSavedUser(userRepository, new Nickname("수신자"), new InvitationCode("EEEEEE"));
            Long mateId = sut.requestMate(requester.getId(), "EEEEEE");
            sut.updateMateStatus(mateId, receiver.getId(), MateStatus.ACCEPTED);

            // when & then
            assertThatThrownBy(() -> sut.updateMateStatus(mateId, receiver.getId(), MateStatus.REJECTED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청만 거절할 수 있습니다.");
        }
    }

    private void acceptMate(Long mateId) {
        Mate mate = mateRepository.findById(mateId).orElseThrow();
        mate.accept();
        mateRepository.save(mate);
    }
}
