package com.example.demo.infrastructure.controller;

import com.example.demo.application.user.UserService;
import com.example.demo.infrastructure.controller.dto.InvitationCodeWebResponse;
import com.example.demo.infrastructure.controller.dto.NicknameWebRequest;
import com.example.demo.infrastructure.interceptor.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/me/nickname")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<InvitationCodeWebResponse> registerNickname(@Parameter(hidden = true) @UserId Long userId,
                                                                      @RequestBody NicknameWebRequest nickNameWebRequest) {
        String invitationCode = userService.registerNickname(userId, nickNameWebRequest.nickname());

        return ResponseEntity.ok(new InvitationCodeWebResponse(invitationCode));
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> withdraw(@UserId Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }
}
