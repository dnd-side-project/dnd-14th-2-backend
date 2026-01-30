package com.example.demo.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.infrastructure.user.RandomUpperInvitationCodeGenerator;
import org.junit.jupiter.api.Test;

class InvitationCodeGeneratorTest {

    InvitationCodeGenerator sut = new RandomUpperInvitationCodeGenerator();

    @Test
    void 초대코드는_항상_6자리이다() {
        // given
        String nickname = "test";

        // when
        String code = sut.generate(nickname);

        // then
        assertThat(code).hasSize(6);
    }

    @Test
    void 초대코드는_영어대문자로만_이루어져있다() {
        // given
        String nickname = "test";

        // when
        String code = sut.generate(nickname);

        // then
        assertThat(code).isUpperCase();
    }
}
