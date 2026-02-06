package com.example.demo.util;

import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;

public class DbUtils {
    public static User kakaoUser(String providerId, Nickname nickname, InvitationCode invitationCode) {
        return new User(
            nickname,
            invitationCode,
            "test@example.com",
            "https://profile.com/image.png",
            Provider.KAKAO,
            providerId
        );
    }

    public static User givenSavedUser(UserRepository repo, String providerId, Nickname nickname, InvitationCode invitationCode) {
        return repo.save(kakaoUser(providerId, nickname, invitationCode));
    }
}
