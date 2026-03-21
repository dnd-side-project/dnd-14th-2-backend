package com.example.demo.infrastructure.controller;

import com.example.demo.application.VerdictService;
import com.example.demo.application.dto.JurorVerdict;
import com.example.demo.application.dto.JurorVerdicts;
import com.example.demo.application.dto.MyVerdicts;
import com.example.demo.infrastructure.controller.dto.JurorVerdictWebResponse;
import com.example.demo.infrastructure.controller.dto.JurorVerdictsWebResponse;
import com.example.demo.infrastructure.controller.dto.MyVerdictsWebResponse;
import com.example.demo.infrastructure.controller.dto.RequestJudgeWebRequest;
import com.example.demo.infrastructure.controller.dto.VerdictJudgeWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
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
public class VerdictController {

    private final VerdictService verdictService;

    @PostMapping("/verdicts")
    public ResponseEntity<Void> requestJudge(@Valid @RequestBody RequestJudgeWebRequest request, @UserId Long userId) {
        verdictService.requestVerdict(request.ledgerEntryId(), userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/verdicts/{id}")
    public ResponseEntity<JurorVerdictWebResponse> judge(@Valid @RequestBody VerdictJudgeWebRequest request,
                                      @PathVariable("id") Long verdictId,
                                      @UserId Long jurorId
    ) {
        verdictService.judge(verdictId, jurorId, request.verdictType());
        JurorVerdict verdict = verdictService.getVerdict(verdictId);

        return ResponseEntity.ok(JurorVerdictWebResponse.from(verdict));
    }

    @GetMapping("/verdicts/my")
    public ResponseEntity<MyVerdictsWebResponse> getMyVerdicts(@UserId Long userId) {
        MyVerdicts myVerdicts = verdictService.getMyVerdicts(userId);

        return ResponseEntity.ok(MyVerdictsWebResponse.from(myVerdicts));
    }

    @GetMapping("/verdicts/juror")
    public ResponseEntity<JurorVerdictsWebResponse> getJurorVerdicts(@UserId Long userId) {
        JurorVerdicts jurorVerdicts = verdictService.getJurorVerdicts(userId);

        return ResponseEntity.ok(JurorVerdictsWebResponse.from(jurorVerdicts));
    }
}
