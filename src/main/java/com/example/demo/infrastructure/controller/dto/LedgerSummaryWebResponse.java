package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.DailySummary;
import com.example.demo.application.dto.UserInfo;

import java.time.LocalDate;
import java.util.List;

public record LedgerSummaryWebResponse(
    UserBlock user,
    DataBlock data
) {
    public static LedgerSummaryWebResponse from(
        UserInfo userInfo,
        LocalDate start,
        LocalDate end,
        List<DailySummary> result
    ) {
        List<DailyLedgerSummary> daily = result.stream()
            .map(r -> new DailyLedgerSummary(r.date(), r.incomeTotal(), r.expenseTotal()))
            .toList();

        UserBlock user = new UserBlock(
            userInfo.userId(),
            userInfo.nickname(),
            userInfo.level(),
            userInfo.profile()
        );

        DataBlock data = new DataBlock(start, end, daily);
        return new LedgerSummaryWebResponse(user, data);
    }

    public record UserBlock(
        Long userId,
        String nickname,
        Integer level,
        String profile
    ) {
    }

    public record DataBlock(
        LocalDate start,
        LocalDate end,
        List<DailyLedgerSummary> daily
    ) {
    }

    public record DailyLedgerSummary(
        LocalDate date,
        long incomeTotal,
        long expenseTotal
    ) {
    }
}
