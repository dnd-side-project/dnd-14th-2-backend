package com.example.demo.domain;

import com.example.demo.domain.enums.MateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.example.demo.util.DbUtils.kakaoUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MateTest {

    private final User requester = createUserWithId(1L, "kakao-1", "요청자", "AAAAAA");
    private final User receiver = createUserWithId(2L, "google-1", "수신자", "BBBBBB");

    private User createUserWithId(Long id, String providerId, String nickname, String code) {
        User user = kakaoUser(providerId, new Nickname(nickname), new InvitationCode(code));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidation {

        @Test
        void 처음_생성시_status는_PENDING() {
            Mate mate = new Mate(requester, receiver);

            assertThat(mate.getRequester()).isEqualTo(requester);
            assertThat(mate.getReceiver()).isEqualTo(receiver);
            assertThat(mate.getStatus()).isEqualTo(MateStatus.PENDING);
        }

        @Test
        void requester가_null이면_예외발생() {
            assertThatThrownBy(() -> new Mate(null, receiver))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("요청자는 필수입니다.");
        }

        @Test
        void receiver가_null이면_예외발생() {
            assertThatThrownBy(() -> new Mate(requester, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수신자는 필수입니다.");
        }

        @Test
        void 자기자신에게_친구요청시_예외() {
            assertThatThrownBy(() -> new Mate(requester, requester))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("accept 검증")
    class AcceptValidation {

        @Test
        void 친구수락시_PENDING에서_ACCEPTED로_변경() {
            Mate mate = new Mate(requester, receiver);

            mate.accept();

            assertThat(mate.getStatus()).isEqualTo(MateStatus.ACCEPTED);
        }

        @Test
        void ACCEPTED에서_친구수락_요청시_예외발생() {
            Mate mate = new Mate(requester, receiver);
            mate.accept();

            assertThatThrownBy(mate::accept)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청만 수락할 수 있습니다.");
        }

        @Test
        void REJECTED에서_친구수락_요청시_예외발생() {
            Mate mate = new Mate(requester, receiver);
            mate.reject();

            assertThatThrownBy(mate::accept)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청만 수락할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("reject 검증")
    class RejectValidation {

        @Test
        void 친구거절시_PENDING에서_REJECTED로_변경() {
            Mate mate = new Mate(requester, receiver);

            mate.reject();

            assertThat(mate.getStatus()).isEqualTo(MateStatus.REJECTED);
        }

        @Test
        void ACCEPTED에서_친구거절_요청시_예외발생() {
            Mate mate = new Mate(requester, receiver);
            mate.accept();

            assertThatThrownBy(mate::reject)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청만 거절할 수 있습니다.");
        }

        @Test
        void REJECTED에서_친구거절_요청시_예외발생() {
            Mate mate = new Mate(requester, receiver);
            mate.reject();

            assertThatThrownBy(mate::reject)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청만 거절할 수 있습니다.");
        }
    }
}
