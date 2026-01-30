package com.example.demo.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 닉네임_첫_등록시_닉네임이_존재하면_예외가_발생한다() {
        // given
        User user = new User("test-email", "test-profile", Provider.GOOGLE, "test-provider-id");
        user.registerNickname(new Nickname("test"));

        // when & then
        Assertions.assertThatThrownBy(() -> user.registerNickname(new Nickname("test2")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 닉네임이 등록된 사용자입니다.");
    }
}
