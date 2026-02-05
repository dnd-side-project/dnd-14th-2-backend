package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.application.user.RandomNicknameGenerator;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NicknameGeneratorTest {

    NicknameGenerator sut = new RandomNicknameGenerator();

    @Test
    void 닉네임을_생성한다() {
        // when
        Nickname nickname = sut.generate();

        // then
        assertThat(nickname).isNotNull();
        assertThat(nickname.value()).isNotBlank();
    }

    @Test
    void 닉네임은_매번_다르게_생성될_수_있다() {
        // given
        NicknameGenerator generator = new RandomNicknameGenerator();

        // when
        Set<String> nicknames = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            nicknames.add(generator.generate().value());
        }

        // then
        // 50번 생성 시 최소 10개 이상은 달라야 함 (랜덤성 확인)
        assertThat(nicknames.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void 닉네임_생성_가능한_조합이_존재한다() {
        // given
        NicknameGenerator generator = new RandomNicknameGenerator();

        // when & then
        // 여러 번 시도해서 예외 없이 생성되는지 확인
        for (int i = 0; i < 100; i++) {
            Nickname nickname = generator.generate();
            assertThat(nickname.value()).isNotBlank();
        }
    }
}
