package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenWebRequest(
    @NotBlank
    String refreshToken
) {
}
