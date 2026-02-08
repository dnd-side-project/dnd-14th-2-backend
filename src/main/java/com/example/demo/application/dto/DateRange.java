package com.example.demo.application.dto;

import java.time.LocalDate;

public record DateRange(LocalDate start, LocalDate end) {
    public static DateRange resolve(LocalDate start, LocalDate end) {
        return between(start, end);
    }

    private static DateRange between(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start는 end보다 클 수 없습니다.");
        }
        return new DateRange(start, end);
    }
}
