package com.example.demo.infrastructure.controller;

import com.example.demo.application.LedgerService;
import com.example.demo.application.dto.LedgerEntriesByDateRangeResponse;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.infrastructure.controller.dto.LedgerDetailWebResponse;
import com.example.demo.infrastructure.controller.dto.LedgerSummaryWebResponse;
import com.example.demo.infrastructure.controller.dto.UpdateLedgerMemoWebRequest;
import com.example.demo.infrastructure.controller.dto.UpsertLedgerWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LedgerController {
    private final LedgerService ledgerService;

    @PostMapping("/ledgers")
    public ResponseEntity<LedgerDetailWebResponse> create(
        @UserId Long userId,
        @Valid @RequestBody UpsertLedgerWebRequest request
    ) {
        UpsertLedgerCommand command = request.toCommand(userId);
        LedgerResult result = ledgerService.createLedgerEntry(command);
        LedgerDetailWebResponse response = LedgerDetailWebResponse.from(result);

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
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate end
    ) {
        LedgerEntriesByDateRangeResponse response = ledgerService.getSummary(userId, start, end);
        return ResponseEntity.ok(LedgerSummaryWebResponse.from(response));
    }
}
