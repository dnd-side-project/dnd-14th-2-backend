package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.infrastructure.user.SecureRandomBytesSource;
import org.junit.jupiter.api.Test;

class InvitationCodeTest {

    @Test
    void 초대코드는_6자리의_영어_대문자로_구성된다() {
        // given
        RandomBytesSource randomBytesSource = new SecureRandomBytesSource();

        // when
        InvitationCode invitationCode = InvitationCode.generate(randomBytesSource);

        // then
        assertThat(invitationCode.value()).isUpperCase();
        assertThat(invitationCode.value()).hasSize(6);
    }

    @Test
    void 영어대문자가_아닌_초대코드를_만들_수_없다() {
        // when & then
        assertThatThrownBy(() -> new InvitationCode("abcdef"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("초대코드는 영어 대문자로 이루어져야 합니다.");
    }

    @Test
    void 초대코드는_6자리_여야만_한다() {
        // when & then
        assertThatThrownBy(() -> new InvitationCode("ABCD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("초대코드는 6자리여야 합니다.");
    }
}
