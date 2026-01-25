package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NicknameTest {

    @ParameterizedTest
    @NullAndEmptySource
    void 닉네임은_비어있을_수_없다(String nickname) {
        // when & then
        assertThatThrownBy(() -> new Nickname(nickname))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("닉네임은 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5자넘는닉네임", "5자가훨씬넘어버리는닉네임"})
    void 닉네임은_5자를_넘을_수_없다(String nickname) {
        // when & then
        assertThatThrownBy(() -> new Nickname(nickname))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("닉네임은 5자 이내여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"A대문자", "-특문", "^특", "&&특문", "BCDA"})
    void 닉네임은_한글_숫자_영어소문자로만_구성되어야_한다(String nickname) {
        // when & then
        assertThatThrownBy(() -> new Nickname(nickname))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("닉네임은 한글, 숫자, 영어 소문자로만 이루어져야 합니다.");
    }
}
