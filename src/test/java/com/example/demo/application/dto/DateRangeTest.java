package com.example.demo.application.dto;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Test
    void start가_end보다_미래면_IllegalArgumentException을_던진다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 1);

        // when & then
        assertThatThrownBy(() -> DateRange.resolve(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("start는 end보다 클 수 없습니다.");
    }

    @Test
    void start가_end보다_과거면_DateRange를_반환한다() {
        // given
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 10);

        // when
        DateRange range = DateRange.resolve(start, end);

        // then
        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(end);
    }
}