package com.example.demo.application;

import com.example.demo.application.dto.DateRange;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Component
public class DateRangeResolver {
    private static final Clock CLOCK = Clock.system(ZoneId.of("Asia/Seoul"));

    public DateRange resolve(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now(CLOCK);

        if (start == null && end == null) {
            YearMonth ym = YearMonth.from(today);
            return new DateRange(ym.atDay(1), ym.atEndOfMonth());
        }
        if (start == null) {
            YearMonth ym = YearMonth.from(end);
            return new DateRange(ym.atDay(1), end);
        }
        if (end == null) {
            YearMonth ym = YearMonth.from(start);
            return new DateRange(start, ym.atEndOfMonth());
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start는 end보다 클 수 없습니다.");
        }
        return new DateRange(start, end);
    }
}
