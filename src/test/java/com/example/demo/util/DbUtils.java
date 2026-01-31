package com.example.demo.util;

import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;

public class DbUtils {
    public static User kakaoUser(Nickname nickname, InvitationCode invitationCode) {
        return new User(
            "test@example.com",
            nickname,
            invitationCode,
            "https://profile.com/image.png",
            Provider.KAKAO,
            "kakao-test-1"
        );
    }

    public static User givenSavedUser(UserRepository repo, Nickname nickname, InvitationCode invitationCode) {
        return repo.save(kakaoUser(nickname, invitationCode));
    }
}
