package com.example.demo.infrastructure.controller;

import com.example.demo.application.MateService;
import com.example.demo.infrastructure.controller.dto.CreateMateWebRequest;
import com.example.demo.infrastructure.controller.dto.MateCreateWebResponse;
import com.example.demo.infrastructure.controller.dto.MateInfoWebResponse;
import com.example.demo.infrastructure.controller.dto.MateReceivedWebResponse;
import com.example.demo.infrastructure.controller.dto.UpdateMateStatusWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;

    @PostMapping("/mates")
    public ResponseEntity<MateCreateWebResponse> create(
            @UserId Long userId,
            @Valid @RequestBody CreateMateWebRequest request
    ) {
        Long mateId = mateService.requestMate(userId, request.invitationCode());
        URI location = URI.create("/mates/" + mateId);
        return ResponseEntity.created(location).body(new MateCreateWebResponse(mateId));
    }

    @GetMapping("/mates")
    public ResponseEntity<List<MateInfoWebResponse>> getAcceptedMates(@UserId Long userId) {
        return ResponseEntity.ok(
                mateService.getAcceptedMates(userId).stream()
                        .map(MateInfoWebResponse::from)
                        .toList()
        );
    }

    @GetMapping("/mates/received")
    public ResponseEntity<List<MateReceivedWebResponse>> getReceivedRequests(@UserId Long userId) {
        return ResponseEntity.ok(
                mateService.getReceivedRequests(userId).stream()
                        .map(MateReceivedWebResponse::from)
                        .toList()
        );
    }

    @PatchMapping("/mates/{mateId}")
    public ResponseEntity<Void> updateMateStatus(
            @UserId Long userId,
            @PathVariable Long mateId,
            @Valid @RequestBody UpdateMateStatusWebRequest request
    ) {
        mateService.updateMateStatus(mateId, userId, request.status());
        return ResponseEntity.ok().build();
    }
}
