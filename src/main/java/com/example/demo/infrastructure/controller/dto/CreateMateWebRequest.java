package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMateWebRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Z]{6}$", message = "올바르지 않은 초대 코드 형식입니다 (영어 대문자 6자리)")
    String invitationCode
) {
}
