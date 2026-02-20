package com.example.demo.infrastructure.controller;

import com.example.demo.application.VerdictService;
import com.example.demo.infrastructure.controller.dto.RequestJudgeWebRequest;
import com.example.demo.infrastructure.controller.dto.VerdictJudgeWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VerdictController {

    private final VerdictService verdictService;

    public ResponseEntity<Void> requestJudge(RequestJudgeWebRequest request, @UserId Long userId) {
        verdictService.requestVerdict(request.ledgerEntryId(), userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> judge(VerdictJudgeWebRequest request, @UserId Long jurorId) {
        verdictService.judge(request.verdictId(), jurorId, request.verdictType());
        return ResponseEntity.ok().build();
    }
}
