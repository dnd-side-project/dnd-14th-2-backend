package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.UserInfo;

public record UserInfoWebResponse(
    Long id,
    String nickname,
    Integer level,
    String invitationCode
) {
    public static UserInfoWebResponse from(UserInfo userInfo) {
        return new UserInfoWebResponse(
            userInfo.userId(),
            userInfo.nickname(),
            userInfo.level(),
            userInfo.invitationCode()
        );
    }
}
