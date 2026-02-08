package com.example.demo.infrastructure.controller;

import com.example.demo.application.dto.UserInfo;
import com.example.demo.application.user.UserService;
import com.example.demo.infrastructure.controller.dto.NicknameWebRequest;
import com.example.demo.infrastructure.controller.dto.UserInfoWebResponse;
import com.example.demo.infrastructure.interceptor.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/me/nickname")
    public ResponseEntity<Void> changeNickname(@UserId Long userId,
                                               @Valid @RequestBody NicknameWebRequest nicknameWebRequest
    ) {
        userService.changeNickname(userId, nicknameWebRequest.nickname());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> withdraw(@UserId Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserInfoWebResponse> getUserInfo(@UserId Long userId) {
        UserInfo userInfo = userService.getUserInfo(userId);
        UserInfoWebResponse response = UserInfoWebResponse.from(userInfo);

        return ResponseEntity.ok(response);
    }
}
