package com.example.demo.application.dto;

import com.example.demo.domain.User;

public record UserInfo(
    Long userId,
    String nickname,
    Integer level,
    String profile
) {
    public static UserInfo from(User user) {
        return new UserInfo(
            user.getId(),
            user.getNickname(),
            user.getLevel(),
            user.getProfile()
        );
    }
}
