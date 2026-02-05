package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.user.SecureRandomBytesSource;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"abcdef", "A$BCDE", "123ABC"})
    void 영어대문자가_아닌_초대코드를_만들_수_없다(String code) {
        // when & then
        assertThatThrownBy(() -> new InvitationCode(code))
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

    @Test
    void 초대코드는_매번_다르게_생성된다() {
        // given
        RandomBytesSource randomBytesSource = new SecureRandomBytesSource();

        // when
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(InvitationCode.generate(randomBytesSource).value());
        }

        // then
        assertThat(codes.size()).isGreaterThan(90); // 중복이 거의 없어야 함
    }

    @Test
    void 동일한_바이트_입력은_동일한_초대코드를_생성한다() {
        // given
        RandomBytesSource fixedSource = length -> new byte[]{0, 1, 2, 3, 4, 5};

        // when
        InvitationCode code1 = InvitationCode.generate(fixedSource);
        InvitationCode code2 = InvitationCode.generate(fixedSource);

        // then
        assertThat(code1.value()).isEqualTo(code2.value());
    }
}
