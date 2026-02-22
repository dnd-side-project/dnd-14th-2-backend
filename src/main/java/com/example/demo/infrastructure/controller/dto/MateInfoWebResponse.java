package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.MateInfo;

public record MateInfoWebResponse(Long mateId, String nickname, String invitationCode, int verdictCount) {

    public static MateInfoWebResponse from(MateInfo info) {
        return new MateInfoWebResponse(info.mateId(), info.nickname(), info.invitationCode(), info.verdictCount());
    }
}
