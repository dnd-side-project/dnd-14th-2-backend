package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.application.user.SecureRandomBytesSource;
import org.junit.jupiter.api.Test;

class RandomBytesSourceTest {

    RandomBytesSource sut = new SecureRandomBytesSource();

    @Test
    void 요청한_길이만큼_바이트_배열을_생성한다() {
        // when
        byte[] bytes = sut.bytes(6);

        // then
        assertThat(bytes).hasSize(6);
    }

}
