package com.example.demo.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMateWebRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{6}$") String invitationCode
) {
}
