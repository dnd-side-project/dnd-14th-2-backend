package com.example.demo.application.dto;

public record UserInfo(
    Long userId,
    String nickname,
    Integer level,
    String profile
) {
}
