package com.example.demo.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.OauthUserInfo;
import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.util.AbstractIntegrationTest;
import com.example.demo.util.DbUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    UserService sut;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("로그인(회원가입)")
    class login {
        @Test
        void 신규_유저_로그인_시_닉네임과_초대코드가_생성되어_저장된다() {
            // given
            OauthUserInfo oauthUserInfo = new OauthUserInfo(
                "google123",
                "newuser@example.com",
                "profile.jpg"
            );

            // when
            User result = sut.findOrCreateUser(Provider.GOOGLE, oauthUserInfo);

            // then
            assertThat(result.getNickname()).isNotNull();
            assertThat(result.getNickname()).isNotEmpty();
            assertThat(result.getInvitationCode()).isNotNull();
            assertThat(result.getInvitationCode().value()).isNotEmpty();

            User savedUser = userRepository.findByProviderAndProviderId(Provider.GOOGLE, "google123")
                .orElseThrow();

            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getNickname()).isNotNull();
            assertThat(savedUser.getInvitationCode()).isNotNull();
        }

        @Test
        void 기존_유저가_다시_로그인하면_기존_정보를_반환한다() {
            // given
            OauthUserInfo oauthUserInfo = new OauthUserInfo(
                "google123",
                "newuser@example.com",
                "profile.jpg"
            );

            User firstUser = sut.findOrCreateUser(Provider.KAKAO, oauthUserInfo);

            // when
            User secondUser = sut.findOrCreateUser(Provider.KAKAO, oauthUserInfo);

            // then
            assertThat(secondUser.getEmail()).isEqualTo(firstUser.getEmail());
            assertThat(secondUser.getId()).isEqualTo(firstUser.getId());
        }

        @Test
        void 여러_유저가_생성되어도_닉네임과_초대코드는_모두_유니크하다() {
            // given
            OauthUserInfo user1 = new OauthUserInfo("id1", "user1@example.com", "pic1.jpg");
            OauthUserInfo user2 = new OauthUserInfo("id2", "user2@example.com", "pic2.jpg");
            OauthUserInfo user3 = new OauthUserInfo("id3", "user3@example.com", "pic3.jpg");

            // when
            User result1 = sut.findOrCreateUser(Provider.GOOGLE, user1);
            User result2 = sut.findOrCreateUser(Provider.GOOGLE, user2);
            User result3 = sut.findOrCreateUser(Provider.GOOGLE, user3);

            // then
            assertThat(result1.getNickname()).isNotEqualTo(result2.getNickname());
            assertThat(result2.getNickname()).isNotEqualTo(result3.getNickname());
            assertThat(result1.getNickname()).isNotEqualTo(result3.getNickname());

            User savedUser1 = userRepository.findByProviderAndProviderId(Provider.GOOGLE, "id1").orElseThrow();
            User savedUser2 = userRepository.findByProviderAndProviderId(Provider.GOOGLE, "id2").orElseThrow();
            User savedUser3 = userRepository.findByProviderAndProviderId(Provider.GOOGLE, "id3").orElseThrow();

            assertThat(savedUser1.getInvitationCode()).isNotEqualTo(savedUser2.getInvitationCode());
            assertThat(savedUser2.getInvitationCode()).isNotEqualTo(savedUser3.getInvitationCode());
            assertThat(savedUser1.getInvitationCode()).isNotEqualTo(savedUser3.getInvitationCode());
        }

        @Test
        void 같은_이메일이라도_Provider가_다르면_다른_유저로_생성된다() {
            // given
            String sameEmail = "same@example.com";
            OauthUserInfo googleUser = new OauthUserInfo("google999", sameEmail, "pic.jpg");
            OauthUserInfo kakaoUser = new OauthUserInfo("kakao999", sameEmail, "pic.jpg");

            // when
            User googleResult = sut.findOrCreateUser(Provider.GOOGLE, googleUser);
            User kakaoResult = sut.findOrCreateUser(Provider.KAKAO, kakaoUser);

            // then
            assertThat(googleResult.getId()).isNotEqualTo(kakaoResult.getId());
            assertThat(googleResult.getNickname()).isNotEqualTo(kakaoResult.getNickname());
        }
    }

    @Nested
    @DisplayName("회원탈퇴")
    class withdraw {
        @Test
        void 회원탈퇴를_할_수_있다() {
            // given
            User user = DbUtils.givenSavedUser(userRepository);

            // when
            sut.withdrawUser(user.getId());

            // then
            assertThat(userRepository.findById(user.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_유저면_예외없이_종료된다() {
            assertThatCode(() -> sut.withdrawUser(999L)).doesNotThrowAnyException();
        }

        @Test
        void 이미_탈퇴한_유저를_다시_탈퇴해도_멱등성이_지켜진다() {
            User user = DbUtils.givenSavedUser(userRepository);

            sut.withdrawUser(user.getId());

            assertThatCode(() -> sut.withdrawUser(user.getId()))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("닉네임 변경")
    class ChangeNickname {
        @Test
        void 사용자는_닉네임_변경을_할_수_있다() {
            // given
            String newNickname = "users";
            User user = DbUtils.givenSavedUser(userRepository);

            // when
            sut.changeNickname(user.getId(), newNickname);

            // then
            assertThat(userRepository.findById(user.getId()))
                .hasValueSatisfying(findUser -> assertThat(findUser.getNickname()).isEqualTo(newNickname));
            assertThat(userRepository.existsByNickname_Value(newNickname)).isTrue();
        }

        @Test
        void 없는_사용자는_닉네임_변경을_할_수_없다() {
            // given
            Long wrongUserId = 999L;
            String newNickname = "test";

            // when
            assertThatThrownBy(() -> sut.changeNickname(wrongUserId, newNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
        }

        @Test
        void 중복_닉네임으로_변경할_수_없다() {
            // given
            String user1Nickname = "test";
            DbUtils.givenSavedUser(userRepository, new Nickname(user1Nickname), new InvitationCode("INCODE"));
            User user2 = DbUtils.givenSavedUser(userRepository);

            // when
            assertThatThrownBy(() -> sut.changeNickname(user2.getId(), user1Nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("중복되는 닉네임입니다.");
        }
    }

    @Test
    void 존재하는_userId로_조회하면_유저_정보를_반환한다() {
        // given
        OauthUserInfo oauthUserInfo = new OauthUserInfo(
            "getinfo123",
            "getinfo@example.com",
            "profile.jpg"
        );
        User created = sut.findOrCreateUser(Provider.GOOGLE, oauthUserInfo);

        // when
        UserInfo result = sut.getUserInfo(created.getId());

        // then
        assertThat(result.userId()).isEqualTo(created.getId());
        assertThat(result.nickname()).isEqualTo(created.getNickname());
        assertThat(result.level()).isEqualTo(created.getLevel());
        assertThat(result.profile()).isEqualTo(created.getProfile());
    }

    @Test
    void 존재하지_않는_유저_ID로_조회하면_예외를_발생시킨다() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo(nonExistentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 사용자입니다.");
        assertThat(userRepository.findById(nonExistentId)).isEmpty();
    }
}
