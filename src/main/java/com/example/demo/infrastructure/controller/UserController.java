package com.example.demo.infrastructure.controller;

import com.example.demo.application.UserService;
import com.example.demo.infrastructure.interceptor.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> withdraw(@UserId Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }
}
