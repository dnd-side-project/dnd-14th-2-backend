package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record DemoLoginWebRequest(
    @NotBlank(message = "deviceId는 필수입니다.")
    String deviceId
) {
}
