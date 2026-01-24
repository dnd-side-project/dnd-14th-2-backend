package com.example.demo.infrastructure.controller.dto;

import com.example.demo.domain.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OauthLoginWebRequest(
    @NotNull Provider provider,
    @NotBlank String code
) {
}
