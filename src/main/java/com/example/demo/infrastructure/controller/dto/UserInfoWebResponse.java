package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.UserInfo;

public record UserInfoWebResponse(
    String nickname
) {
    public static UserInfoWebResponse from(UserInfo userInfo) {
        return new UserInfoWebResponse(userInfo.nickname());
    }
}
