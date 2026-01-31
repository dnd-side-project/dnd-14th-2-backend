package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record NicknameWebRequest(
    @NotBlank(message = "닉네임은 비어있을 수 없습니다.")
    String nickname
) {
}
