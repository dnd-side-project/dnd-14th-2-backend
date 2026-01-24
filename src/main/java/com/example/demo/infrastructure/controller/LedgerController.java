package com.example.demo.infrastructure.controller;

import com.example.demo.application.DateRangeResolver;
import com.example.demo.application.LedgerService;
import com.example.demo.application.UserService;
import com.example.demo.application.dto.*;
import com.example.demo.infrastructure.controller.dto.*;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LedgerController {
    private final LedgerService ledgerService;
    private final UserService userService;
    private final DateRangeResolver dateRangeResolver;

    @PostMapping("/ledgers")
    public ResponseEntity<CreateLedgerWebResponse> create(
        @UserId Long userId,
        @Valid @RequestBody UpsertLedgerWebRequest request
    ) {
        UpsertLedgerCommand command = request.toCommand(userId);
        LedgerResult result = ledgerService.createLedgerEntry(command);
        CreateLedgerWebResponse response = new CreateLedgerWebResponse(result.ledgerId());

        URI location = URI.create("/ledgers/" + response.ledgerId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/ledgers/{ledgerId}")
    public ResponseEntity<LedgerDetailWebResponse> getById(
        @UserId Long userId,
        @PathVariable Long ledgerId
    ) {
        LedgerResult result = ledgerService.getLedgerEntry(userId, ledgerId);
        LedgerDetailWebResponse response = LedgerDetailWebResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/ledgers/{ledgerId}/memo")
    public ResponseEntity<Void> updateMemo(
        @UserId Long userId,
        @PathVariable Long ledgerId,
        @Valid @RequestBody UpdateLedgerMemoWebRequest request
    ) {
        ledgerService.updateLedgerMemo(userId, ledgerId, request.memo());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ledgers/{ledgerId}")
    public ResponseEntity<LedgerDetailWebResponse> update(
        @UserId Long userId,
        @PathVariable Long ledgerId,
        @Valid @RequestBody UpsertLedgerWebRequest request
    ) {
        UpsertLedgerCommand command = request.toCommand(userId);
        LedgerResult result = ledgerService.updateLedgerEntry(ledgerId, command);
        LedgerDetailWebResponse response = LedgerDetailWebResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/ledgers/{ledgerId}")
    public ResponseEntity<Void> delete(
        @UserId Long userId,
        @PathVariable Long ledgerId
    ) {
        ledgerService.deleteLedgerEntry(userId, ledgerId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ledgers/summary")
    public ResponseEntity<LedgerSummaryWebResponse> getSummary(
        @UserId Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        DateRange range = dateRangeResolver.resolve(start, end);
        UserInfo userInfo = userService.getUserInfo(userId);
        List<DailySummary> result = ledgerService.getSummary(userId, range.start(), range.end());
        return ResponseEntity.ok(LedgerSummaryWebResponse.from(userInfo, range.start(), range.end(), result));
    }

    @GetMapping("/ledgers/daily")
    public ResponseEntity<DailyLedgerDetailWebResponse> getDailyDetail(
        @UserId Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = dateRangeResolver.resolveDate(date);
        DailyLedgerDetail result = ledgerService.getLedgerEntriesByDate(userId, targetDate);
        DailyLedgerDetailWebResponse response = new DailyLedgerDetailWebResponse(result);

        return ResponseEntity.ok(response);
    }
}
