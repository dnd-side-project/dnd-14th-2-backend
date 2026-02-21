package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.MateReceivedInfo;

public record MateReceivedWebResponse(Long mateId, String nickname, String invitationCode) {

    public static MateReceivedWebResponse from(MateReceivedInfo info) {
        return new MateReceivedWebResponse(info.mateId(), info.nickname(), info.invitationCode());
    }
}
