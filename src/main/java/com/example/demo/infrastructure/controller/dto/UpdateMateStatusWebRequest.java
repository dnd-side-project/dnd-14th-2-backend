package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.enums.MateStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMateStatusWebRequest(@NotNull MateStatus status) {
}
