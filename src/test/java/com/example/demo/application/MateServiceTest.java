package com.example.demo.application;

import static com.example.demo.util.DbUtils.givenSavedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.MateInfo;
import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Mate;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.MateStatus;
import com.example.demo.util.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MateServiceTest extends AbstractIntegrationTest {

    @Autowired
    MateService sut;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MateRepository mateRepository;

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

        private void acceptMate(Long mateId) {
            Mate mate = mateRepository.findById(mateId).orElseThrow();
            mate.accept();
            mateRepository.save(mate);
        }
    }
}
