package com.example.demo.infrastructure.controller;

import com.example.demo.application.MateService;
import com.example.demo.infrastructure.controller.dto.CreateMateWebRequest;
import com.example.demo.infrastructure.controller.dto.MateCreateWebResponse;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
