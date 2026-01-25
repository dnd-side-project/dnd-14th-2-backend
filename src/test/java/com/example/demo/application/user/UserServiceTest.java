package com.example.demo.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.example.demo.domain.Provider;
import com.example.demo.application.user.UserService;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.util.AbstractIntegrationTest;
import com.example.demo.util.DbUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    UserService sut;

    @Autowired
    UserRepository userRepository;

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
