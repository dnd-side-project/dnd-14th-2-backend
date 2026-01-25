package com.example.demo.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.user.RandomUpperInvitationCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

class InvitationCodeGeneratorTest {

    @Value("${invitation.secret-salt}")
    String salt;

    InvitationCodeGenerator sut = new RandomUpperInvitationCodeGenerator(salt);

    @Test
    void 초대코드는_항상_6자리이다() {
        // given
        User user = new User("email", "profile", Provider.GOOGLE, "provider-id");
        user.registerNickname("test");

        // when
        String code = sut.generate(user);

        // then
        assertThat(code).hasSize(6);
    }

    @Test
    void 초대코드는_영어대문자로만_이루어져있다() {
        // given
        User user = new User("email", "profile", Provider.GOOGLE, "provider-id");
        user.registerNickname("test");

        // when
        String code = sut.generate(user);

        // then
        assertThat(code).isUpperCase();
    }
}
